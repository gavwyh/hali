# Multi-stage build for C++ log sidecar
FROM alpine:3.18 AS builder

# Install build dependencies
RUN apk add --no-cache \
    build-base \
    cmake \
    curl-dev \
    nlohmann-json \
    pkgconfig \
    linux-headers

WORKDIR /build

# Copy source code
COPY CMakeLists.txt .
COPY src/ src/

# Build the application
RUN mkdir build && cd build && \
    cmake .. && \
    make -j$(nproc) && \
    strip log-sidecar

# Runtime stage
FROM alpine:3.18

# Install runtime dependencies
RUN apk add --no-cache \
    libcurl \
    libstdc++ \
    && addgroup -g 1001 sidecar \
    && adduser -D -u 1001 -G sidecar sidecar

# Copy binary from builder stage
COPY --from=builder /build/build/log-sidecar /usr/local/bin/log-sidecar

# Create log directory
RUN mkdir -p /var/log/app && chown sidecar:sidecar /var/log/app

# Switch to non-root user
USER sidecar

# Expose metrics port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/metrics || exit 1

ENTRYPOINT ["/usr/local/bin/log-sidecar"]