#ifndef LOG_WATCHER_H
#define LOG_WATCHER_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <queue>
#include "config.h"
#include "loki_client.h"
#include "metrics.h"
#include "log_entry.h"

class LogWatcher {
public:
    explicit LogWatcher(const Config& config);
    ~LogWatcher();

    void start();
    void stop();

private:
    void watch_logs();
    void process_log_batch();
    bool parse_log_line(const std::string& line, LogEntry& entry);
    void setup_epoll();
    void add_file_to_watch(const std::string& filepath);

    Config config_;
    std::unique_ptr<LokiClient> loki_client_;
    std::unique_ptr<Metrics> metrics_;

    std::atomic<bool> running_;
    std::thread watcher_thread_;
    std::thread processor_thread_;

    int epoll_fd_;
    std::vector<int> file_descriptors_;

    std::queue<LogEntry> log_queue_;
    std::mutex queue_mutex_;
    std::condition_variable queue_cv_;

    // File position tracking
    std::map<std::string, size_t> file_positions_;
};

#endif
