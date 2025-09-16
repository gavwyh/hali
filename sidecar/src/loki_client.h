#ifndef LOKI_CLIENT_H
#define LOKI_CLIENT_H

#include <string>
#include <vector>
#include "log_entry.h"

class LokiClient {
public:
    explicit LokiClient(const std::string& endpoint);
    ~LokiClient();

    void send_logs(const std::vector<LogEntry>& logs);

private:
    std::string build_loki_payload(const std::vector<LogEntry>& logs);
    bool send_http_request(const std::string& payload);

    std::string endpoint_;
    std::string push_url_;
};

#endif
