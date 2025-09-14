#include <iostream>
#include <cstdlib>
#include <thread>
#include <memory>
#include <signal.h>
#include "log_watcher.h"
#include "metrics_server.h"
#include "config.h"

std::unique_ptr<LogWatcher> g_log_watcher;
std::unique_ptr<MetricsServer> g_metrics_server;

/** 
* Handles system signals for graceful shutdown.
* It stops the LogWatcher and MetricsServer threads before 
* exiting to flush all logs and clean up resources.
* @param signum The signal number received (e.g., SIGINT, SIGTERM).
*/
void signal_handler(int signum) {
    std::cout << "Interrupt signal (" << signum << ") received." << std::endl;
    if (g_log_watcher) {
        g_log_watcher->Stop();
    }
    if (g_metrics_server) {
        g_metrics_server->Stop();
    }
    exit(signum);
}

/**
* Main entry point for the log-processing sidecar.
* Initializes and starts the LogWatcher and MetricsServer components.
* @param argc No. of CLI args.
* @param argv Array of CLI args.
* @return 0 on successful execution, 1 on failure.
*/
int main(int argc, char* argv[]) {
    // Setup signal handlers for graceful shutdown
    signal(SIGINT, signal_handler);
    signal(SIGTERM, signal_handler);
    
    try {
        // Load config
        Config config;
        if (!config.load_from_env()) {
            std::cerr << "Failed to load configuration" << std::endl;
            return 1;
        }
        
        std::cout << "Starting log processing sidecar..." << std::endl;
        std::cout << "Log directory: " << config.log_directory << std::endl;
        std::cout << "Loki endpoint: " << config.loki_endpoint << std::endl;
        std::cout << "Metrics port: " << config.metrics_port << std::endl;
        
        // Run metrics server on another thread 
        g_metrics_server = std::make_unique<MetricsServer>(config.metrics_port);
        std::thread metrics_thread([&]() {
            g_metrics_server->start();
        });
        
        // Initialize log watcher
        g_log_watcher = std::make_unique<LogWatcher>(config);
        
        // Start log watching (blocking)
        g_log_watcher->start();
        
        // Wait for metrics thread
        if (metrics_thread.joinable()) {
            metrics_thread.join();
        }
        
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    
    return EXIT_SUCCESS;
}