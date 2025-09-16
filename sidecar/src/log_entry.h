#ifndef LOG_ENTRY_H
#define LOG_ENTRY_H
#include <string>

struct LogEntry {
    std::string timestamp;
    std::string level;
    std::string message;
    std::string logger;
    std::string thread;
    std::string service;
    std::string namespace_name;

    std::string to_json() const;
};

#endif
