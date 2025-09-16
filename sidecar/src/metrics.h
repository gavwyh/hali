#pragma once
#include <atomic>
#include <string>
#include <map>
#include <mutex>
#include <chrono>

class Metrics {
public:
    Metrics();
    
    void increment_logs_processed();
    void increment_logs_sent(size_t count);
    void increment_errors();
    void set_last_processed_timestamp();
    
    std::string get_prometheus_metrics() const;
    
private:
    std::atomic<uint64_t> logs_processed_total_;
    std::atomic<uint64_t> logs_sent_total_;
    std::atomic<uint64_t> errors_total_;
    std::atomic<uint64_t> last_processed_timestamp_;
    
    mutable std::mutex metrics_mutex_;
    std::chrono::steady_clock::time_point start_time_;
};