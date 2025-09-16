cmake_minimum_required(VERSION 3.16)
project(log-sidecar)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

find_package(PkgConfig REQUIRED)
find_package(Threads REQUIRED)

find_package(CURL REQUIRED)

find_package(nlohmann_json REQUIRED)

add_executable(log-sidecar
    src/main.cpp
    src/log_watcher.cpp
    src/loki_client.cpp
    src/metrics.cpp
    src/metrics_server.cpp
)

target_include_directories(log-sidecar PRIVATE src)

target_link_libraries(log-sidecar
    PRIVATE
    Threads::Threads
    CURL::libcurl
    nlohmann_json::nlohmann_json
)

target_compile_options(log-sidecar PRIVATE -Wall -Wextra -O2)

install(TARGETS log-sidecar DESTINATION bin)
