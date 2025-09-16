#include "log_watcher.h"
#include <unistd.h>
#include <fcntl.h>
#include <filesystem>
#include <iostream>
#include <chrono>
#include <nlohmann/json.hpp>
#include <sys/epoll.h>
#include <sys/inotify.h>

using json = nlohmann::json;
namespace fs = std::filesystem;

LogWatcher::LogWatcher(const Config& config, Metrics& shared_metrics)
    : config_(config),
      loki_client_(std::make_unique<LokiClient>(config_.loki_endpoint)),
      metrics_(shared_metrics),
      running_(false),
      epoll_fd_(-1) {
}


LogWatcher::~LogWatcher() {
    stop();

    if (epoll_fd_ != -1) {
        close(epoll_fd_);
    }

    for (int fd : file_descriptors_) {
        close(fd);
    }
}

void LogWatcher::start() {
    if (running_.load()) {
        return;
    }

    running_.store(true);
    setup_epoll();

    // Start processor thread
    processor_thread_ = std::thread(&LogWatcher::process_log_batch, this);

    // Start watcher thread
    watcher_thread_ = std::thread(&LogWatcher::watch_logs, this);

    std::cout << "Log watcher started" << std::endl;

    // Wait for threads
    if (watcher_thread_.joinable()) {
        watcher_thread_.join();
    }

    if (processor_thread_.joinable()) {
        processor_thread_.join();
    }
}

void LogWatcher::stop() {
    running_.store(false);
    queue_cv_.notify_all();
}

void LogWatcher::setup_epoll() {
    epoll_fd_ = epoll_create1(EPOLL_CLOEXEC);
    if (epoll_fd_ == -1) {
        throw std::runtime_error("Failed to create epoll instance");
    }

    // Watch for new log files and existing ones
    if (fs::exists(config_.log_directory)) {
        for (const auto& entry : fs::directory_iterator(config_.log_directory)) {
            if (entry.is_regular_file() && entry.path().extension() == ".log") {
                add_file_to_watch(entry.path().string());
            }
        }
    }
}

void LogWatcher::add_file_to_watch(const std::string& filepath) {
    int fd = open(filepath.c_str(), O_RDONLY | O_NONBLOCK);
    if (fd == -1) {
        std::cerr << "Failed to open file: " << filepath << std::endl;
        return;
    }

    // Seek to end of file to only read new content
    lseek(fd, 0, SEEK_END);
    file_positions_[filepath] = lseek(fd, 0, SEEK_CUR);

    struct epoll_event event;
    event.events = EPOLLIN | EPOLLPRI;
    event.data.fd = fd;

    if (epoll_ctl(epoll_fd_, EPOLL_CTL_ADD, fd, &event) == -1) {
        std::cerr << "Failed to add file to epoll: " << filepath << std::endl;
        close(fd);
        return;
    }

    file_descriptors_.push_back(fd);
    std::cout << "Watching file: " << filepath << std::endl;
}

void LogWatcher::watch_logs() {
    const int MAX_EVENTS = 10;
    struct epoll_event events[MAX_EVENTS];

    while (running_.load()) {
        int num_events = epoll_wait(epoll_fd_, events, MAX_EVENTS, 1000); // 1 second timeout

        if (num_events == -1) {
            if (errno == EINTR) continue;
            std::cerr << "epoll_wait failed" << std::endl;
            break;
        }

        for (int i = 0; i < num_events; ++i) {
            int fd = events[i].data.fd;

            // Read new content from file
            char buffer[8192];
            ssize_t bytes_read;
            std::string accumulated_line;

            while ((bytes_read = read(fd, buffer, sizeof(buffer))) > 0) {
                std::string content(buffer, bytes_read);
                accumulated_line += content;

                // Process complete lines
                size_t pos = 0;
                while ((pos = accumulated_line.find('\n')) != std::string::npos) {
                    std::string line = accumulated_line.substr(0, pos);
                    accumulated_line.erase(0, pos + 1);

                    if (!line.empty()) {
                        LogEntry entry;
                        if (parse_log_line(line, entry)) {
                            std::lock_guard<std::mutex> lock(queue_mutex_);
                            log_queue_.push(entry);
                            queue_cv_.notify_one();

                            metrics_->increment_logs_processed();
                        }
                    }
                }
            }
        }
    }
}

bool LogWatcher::parse_log_line(const std::string& line, LogEntry& entry) {
    try {
        // Try to parse as JSON first (structured logging)
        json log_json = json::parse(line);

        entry.timestamp = log_json.value("timestamp", "");
        entry.level = log_json.value("level", "INFO");
        entry.message = log_json.value("message", "");
        entry.logger = log_json.value("logger", "");
        entry.thread = log_json.value("thread", "");
        entry.service = config_.service_name;
        entry.namespace_name = config_.namespace_name;

        return true;

    } catch (const json::parse_error&) {
        // Fallback to plain text parsing
        entry.timestamp = std::to_string(
            std::chrono::duration_cast<std::chrono::nanoseconds>(
                std::chrono::system_clock::now().time_since_epoch()
            ).count()
        );
        entry.level = "INFO";
        entry.message = line;
        entry.logger = "unknown";
        entry.thread = "main";
        entry.service = config_.service_name;
        entry.namespace_name = config_.namespace_name;

        return true;
    }
}

void LogWatcher::process_log_batch() {
    std::vector<LogEntry> batch;
    batch.reserve(config_.batch_size);

    while (running_.load()) {
        std::unique_lock<std::mutex> lock(queue_mutex_);

        // Wait for logs or timeout
        queue_cv_.wait_for(lock, std::chrono::milliseconds(config_.flush_interval_ms),
                          [this] { return !log_queue_.empty() || !running_.load(); });

        if (!running_.load() && log_queue_.empty()) {
            break;
        }

        // Collect batch
        while (!log_queue_.empty() && batch.size() < config_.batch_size) {
            batch.push_back(log_queue_.front());
            log_queue_.pop();
        }

        lock.unlock();

        // Send batch to Loki
        if (!batch.empty()) {
            try {
                loki_client_->send_logs(batch);
                metrics_->increment_logs_sent(batch.size());
                std::cout << "Sent " << batch.size() << " logs to Loki" << std::endl;
            } catch (const std::exception& e) {
                std::cerr << "Failed to send logs to Loki: " << e.what() << std::endl;
                metrics_->increment_errors();
            }

            batch.clear();
        }
    }
}

std::string LogEntry::to_json() const {
    json j;
    j["timestamp"] = timestamp;
    j["level"] = level;
    j["message"] = message;
    j["logger"] = logger;
    j["thread"] = thread;
    j["service"] = service;
    j["namespace"] = namespace_name;
    return j.dump();
}
