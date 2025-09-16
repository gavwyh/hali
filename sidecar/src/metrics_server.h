#pragma once
#include <string>
#include <atomic>
#include <thread>
#include <memory>

class Metrics;

class MetricsServer {
public:
    explicit MetricsServer(int port);
    ~MetricsServer();
    
    void start();
    void stop();
    
private:
    void run_server();
    std::string handle_metrics_request();
    
    int port_;
    std::atomic<bool> running_;
    std::thread server_thread_;
    int server_socket_;
};