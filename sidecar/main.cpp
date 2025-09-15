#include <iostream>
#include <cstdlib>
#include <thread>
#include <memory>
#include <signal.h>
#include <atomic>
#include "log_watcher.h"
#include "metrics_server.h"
#include "config.h"

// Atomic flag for shutdown signaling
std::atomic<bool> g_shutdown_requested(false);

std::unique_ptr<LogWatcher> g_log_watcher;
std::unique_ptr<MetricsServer> g_metrics_server;

/** 
* Handles system signals for graceful shutdown.
* It stops the LogWatcher and MetricsServer threads before 
* exiting to flush all logs and clean up resources.
* @param signum The signal number received (e.g., SIGINT, SIGTERM).
*/
void signal_handler(int signum) {
    g_shutdown_requested.store(true);
    if (g_log_watcher) {
        g_log_watcher->Stop();
    }
    if (g_metrics_server) {
        g_metrics_server->Stop();
    }
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
        
        // Start metrics server on another thread to run concurrently with log watcher
        g_metrics_server = std::make_unique<MetricsServer>(config.metrics_port);
        std::thread metrics_thread([&]() {
            g_metrics_server->start();
        }); 
        
        g_log_watcher = std::make_unique<LogWatcher>(config);
        
        // Block and watch for logs
        g_log_watcher->start();
        
        if (metrics_thread.joinable()) {
            metrics_thread.join();
        }
        
        std::cout << "Sidecar shut down gracefully." << std::endl;

    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    
    return EXIT_SUCCESS;
}