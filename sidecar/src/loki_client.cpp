#include "loki_client.h"
#include <curl/curl.h>
#include <nlohmann/json.hpp>
#include <iostream>
#include <chrono>

using json = nlohmann::json;

LokiClient::LokiClient(const std::string& endpoint) : endpoint_(endpoint) {
    push_url_ = endpoint_ + "/loki/api/v1/push";
    curl_global_init(CURL_GLOBAL_DEFAULT);
}

LokiClient::~LokiClient() {
    curl_global_cleanup();
}

void LokiClient::send_logs(const std::vector<LogEntry>& logs) {
    if (logs.empty()) {
        return;
    }

    std::string payload = build_loki_payload(logs);
    if (!send_http_request(payload)) {
        throw std::runtime_error("Failed to send logs to Loki");
    }
}

std::string LokiClient::build_loki_payload(const std::vector<LogEntry>& logs) {
    json payload;
    json streams = json::array();

    // Group logs by labels
    std::map<std::string, std::vector<std::pair<std::string, std::string>>> grouped_logs;

    for (const auto& log : logs) {
        // Create label set
        json labels;
        labels["service"] = log.service;
        labels["namespace"] = log.namespace_name;
        labels["level"] = log.level;
        labels["logger"] = log.logger;

        std::string label_key = labels.dump();

        // Convert timestamp to nanoseconds
        std::string timestamp_ns;
        if (log.timestamp.empty()) {
            timestamp_ns = std::to_string(
                std::chrono::duration_cast<std::chrono::nanoseconds>(
                    std::chrono::system_clock::now().time_since_epoch()
                ).count()
            );
        } else {
            timestamp_ns = log.timestamp;
        }

        grouped_logs[label_key].emplace_back(timestamp_ns, log.to_json());
    }

    // Build streams
    for (const auto& [labels_str, entries] : grouped_logs) {
        json stream;
        stream["stream"] = json::parse(labels_str);

        json values = json::array();
        for (const auto& [timestamp, message] : entries) {
            values.push_back({timestamp, message});
        }
        stream["values"] = values;

        streams.push_back(stream);
    }

    payload["streams"] = streams;
    return payload.dump();
}

static size_t WriteCallback(void* contents, size_t size, size_t nmemb, void* userp) {
    ((std::string*)userp)->append((char*)contents, size * nmemb);
    return size * nmemb;
}

bool LokiClient::send_http_request(const std::string& payload) {
    CURL* curl = curl_easy_init();
    if (!curl) {
        return false;
    }

    std::string response;
    struct curl_slist* headers = nullptr;

    // Set headers
    headers = curl_slist_append(headers, "Content-Type: application/json");

    curl_easy_setopt(curl, CURLOPT_URL, push_url_.c_str());
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, payload.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30L);

    CURLcode res = curl_easy_perform(curl);
    long response_code;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    if (res != CURLE_OK) {
        std::cerr << "CURL error: " << curl_easy_strerror(res) << std::endl;
        return false;
    }

    if (response_code < 200 || response_code >= 300) {
        std::cerr << "HTTP error: " << response_code << ", response: " << response << std::endl;
        return false;
    }

    return true;
}
