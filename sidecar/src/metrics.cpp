#include "metrics.h"
#include <sstream>
#include <chrono>

Metrics::Metrics() 
    : logs_processed_total_(0)
    , logs_sent_total_(0)
    , errors_total_(0)
    , last_processed_timestamp_(0)
    , start_time_(std::chrono::steady_clock::now()) {
}

void Metrics::increment_logs_processed() {
    logs_processed_total_.fetch_add(1, std::memory_order_relaxed);
    set_last_processed_timestamp();
}

void Metrics::increment_logs_sent(size_t count) {
    logs_sent_total_.fetch_add(count, std::memory_order_relaxed);
}

void Metrics::increment_errors() {
    errors_total_.fetch_add(1, std::memory_order_relaxed);
}

void Metrics::set_last_processed_timestamp() {
    auto now = std::chrono::duration_cast<std::chrono::seconds>(
        std::chrono::system_clock::now().time_since_epoch()
    ).count();
    last_processed_timestamp_.store(now, std::memory_order_relaxed);
}

std::string Metrics::get_prometheus_metrics() const {
    std::lock_guard<std::mutex> lock(metrics_mutex_);
    
    auto uptime = std::chrono::duration_cast<std::chrono::seconds>(
        std::chrono::steady_clock::now() - start_time_
    ).count();
    
    std::ostringstream oss;
    
    // Help and type information
    oss << "# HELP log_sidecar_logs_processed_total Total number of log lines processed\n";
    oss << "# TYPE log_sidecar_logs_processed_total counter\n";
    oss << "log_sidecar_logs_processed_total " << logs_processed_total_.load() << "\n\n";
    
    oss << "# HELP log_sidecar_logs_sent_total Total number of log lines sent to Loki\n";
    oss << "# TYPE log_sidecar_logs_sent_total counter\n";
    oss << "log_sidecar_logs_sent_total " << logs_sent_total_.load() << "\n\n";
    
    oss << "# HELP log_sidecar_errors_total Total number of errors encountered\n";
    oss << "# TYPE log_sidecar_errors_total counter\n";
    oss << "log_sidecar_errors_total " << errors_total_.load() << "\n\n";
    
    oss << "# HELP log_sidecar_last_processed_timestamp Unix timestamp of last processed log\n";
    oss << "# TYPE log_sidecar_last_processed_timestamp gauge\n";
    oss << "log_sidecar_last_processed_timestamp " << last_processed_timestamp_.load() << "\n\n";
    
    oss << "# HELP log_sidecar_uptime_seconds Uptime of the log sidecar in seconds\n";
    oss << "# TYPE log_sidecar_uptime_seconds gauge\n";
    oss << "log_sidecar_uptime_seconds " << uptime << "\n\n";
    
    // Processing rate (logs per second)
    double processing_rate = uptime > 0 ? static_cast<double>(logs_processed_total_.load()) / uptime : 0.0;
    oss << "# HELP log_sidecar_processing_rate_per_second Current log processing rate per second\n";
    oss << "# TYPE log_sidecar_processing_rate_per_second gauge\n";
    oss << "log_sidecar_processing_rate_per_second " << processing_rate << "\n\n";
    
    return oss.str();
}