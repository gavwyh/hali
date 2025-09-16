#include "metrics_server.h"
#include "metrics.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <cstring>

extern std::unique_ptr<Metrics> g_metrics;

MetricsServer::MetricsServer(int port) 
    : port_(port), running_(false), server_socket_(-1) {
}

MetricsServer::~MetricsServer() {
    stop();
}

void MetricsServer::start() {
    if (running_.load()) {
        return;
    }
    
    running_.store(true);
    server_thread_ = std::thread(&MetricsServer::run_server, this);
    std::cout << "Metrics server started on port " << port_ << std::endl;
}

void MetricsServer::stop() {
    running_.store(false);
    
    if (server_socket_ != -1) {
        close(server_socket_);
        server_socket_ = -1;
    }
    
    if (server_thread_.joinable()) {
        server_thread_.join();
    }
}

void MetricsServer::run_server() {
    server_socket_ = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket_ == -1) {
        std::cerr << "Failed to create socket" << std::endl;
        return;
    }
    
    int opt = 1;
    if (setsockopt(server_socket_, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        std::cerr << "Failed to set socket options" << std::endl;
        close(server_socket_);
        return;
    }
    
    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(port_);
    
    if (bind(server_socket_, (struct sockaddr*)&address, sizeof(address)) < 0) {
        std::cerr << "Failed to bind socket to port " << port_ << std::endl;
        close(server_socket_);
        return;
    }
    
    if (listen(server_socket_, 3) < 0) {
        std::cerr << "Failed to listen on socket" << std::endl;
        close(server_socket_);
        return;
    }
    
    while (running_.load()) {
        struct sockaddr_in client_addr;
        socklen_t client_len = sizeof(client_addr);
        
        int client_socket = accept(server_socket_, (struct sockaddr*)&client_addr, &client_len);
        if (client_socket < 0) {
            if (running_.load()) {
                std::cerr << "Failed to accept connection" << std::endl;
            }
            continue;
        }
        
        // Read HTTP request
        char buffer[1024] = {0};
        read(client_socket, buffer, 1024);
        
        // Check if it's a GET request to /metrics
        std::string request(buffer);
        if (request.find("GET /metrics") != std::string::npos) {
            std::string response = handle_metrics_request();
            
            std::ostringstream http_response;
            http_response << "HTTP/1.1 200 OK\r\n";
            http_response << "Content-Type: text/plain; version=0.0.4; charset=utf-8\r\n";
            http_response << "Content-Length: " << response.length() << "\r\n";
            http_response << "Connection: close\r\n";
            http_response << "\r\n";
            http_response << response;
            
            std::string full_response = http_response.str();
            send(client_socket, full_response.c_str(), full_response.length(), 0);
        } else {
            // 404 for other paths
            std::string not_found = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";
            send(client_socket, not_found.c_str(), not_found.length(), 0);
        }
        
        close(client_socket);
    }
}

std::string MetricsServer::handle_metrics_request() {
    // Access global metrics instance
    extern std::unique_ptr<Metrics> g_metrics;
    if (g_metrics) {
        return g_metrics->get_prometheus_metrics();
    }
    return "# No metrics available\n";
}