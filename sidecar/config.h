#ifndef CONFIG_H
#define CONFIG_H
#include <string>
#include <cstdlib>

struct Config {
    std::string log_directory{"/var/log/app"};
    std::string loki_endpoint{"http://loki:3100"};
    std::string service_name{"unknown"};
    std::string namespace_name{"default"};
    int metrics_port{9090};
    int batch_size{100};
    int flush_interval_ms{5000};
    
    bool load_from_env() noexcept {
        const char* env_val;
        
        if ((env_val = std::getenv("LOG_DIRECTORY"))) {
            log_directory = env_val;
        }
        
        if ((env_val = std::getenv("LOKI_ENDPOINT"))) {
            loki_endpoint = env_val;
        }
        
        if ((env_val = std::getenv("SERVICE_NAME"))) {
            service_name = env_val;
        }
        
        if ((env_val = std::getenv("NAMESPACE"))) {
            namespace_name = env_val;
        }
        
        if ((env_val = std::getenv("METRICS_PORT"))) {
            try {
                metrics_port = std::stoi(env_val);
            } catch (const std::exception& e) {
                std::cerr << "Warning: Invalid METRICS_PORT='" << env_val
                          << "', using default " << metrics_port << "\n";
            }
        }
        
        if ((env_val = std::getenv("BATCH_SIZE"))) {
            try {
                batch_size = std::stoi(env_val);
            } catch (const std::exception& e) {
                std::cerr << "Warning: Invalid BATCH_SIZE='" << env_val
                          << "', using default " << batch_size << "\n";
            }
        }
        
        if ((env_val = std::getenv("FLUSH_INTERVAL_MS"))) {
            try {
                flush_interval_ms = std::stoi(env_val);
            } catch (const std::exception& e) {
                std::cerr << "Warning: Invalid FLUSH_INTERVAL_MS='" << env_val
                          << "', using default " << flush_interval_ms << "\n";
            }
        }
        
        return !service_name.empty();
    }
};

#endif