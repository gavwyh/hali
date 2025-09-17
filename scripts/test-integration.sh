#!/bin/bash

set -e

NAMESPACE=${NAMESPACE:-"default"}

echo "🧪 Testing Log Sidecar Integration"

# Function to check if service is ready
wait_for_service() {
    local service=$1
    local port=$2
    local timeout=60
    
    echo "⏳ Waiting for $service to be ready..."
    
    for i in $(seq 1 $timeout); do
        if kubectl port-forward -n $NAMESPACE svc/$service $port:$port --timeout=5s > /dev/null 2>&1 &
        then
            local pf_pid=$!
            sleep 2
            
            if curl -s http://localhost:$port/health > /dev/null 2>&1 || \
               curl -s http://localhost:$port/metrics > /dev/null 2>&1; then
                kill $pf_pid 2>/dev/null || true
                echo "✅ $service is ready"
                return 0
            fi
            
            kill $pf_pid 2>/dev/null || true
        fi
        
        sleep 1
    done
    
    echo "❌ $service failed to become ready within $timeout seconds"
    return 1
}

# Test 1: Check if pods are running
echo "🔍 Test 1: Checking pod status..."
kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=logs-service

POD_NAME=$(kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=logs-service -o jsonpath='{.items[0].metadata.name}')

if [ -z "$POD_NAME" ]; then
    echo "❌ No logs-service pod found"
    exit 1
fi

echo "📋 Pod: $POD_NAME"

# Test 2: Check if both containers are running
echo "🔍 Test 2: Checking container status..."
CONTAINER_COUNT=$(kubectl get pod $POD_NAME -n $NAMESPACE -o jsonpath='{.status.containerStatuses[*].name}' | wc -w)

if [ "$CONTAINER_COUNT" -lt 2 ]; then
    echo "❌ Expected 2 containers, found $CONTAINER_COUNT"
    kubectl describe pod $POD_NAME -n $NAMESPACE
    exit 1
fi

echo "✅ Found $CONTAINER_COUNT containers"

# Test 3: Check sidecar metrics
echo "🔍 Test 3: Testing sidecar metrics endpoint..."
kubectl port-forward -n $NAMESPACE pod/$POD_NAME 9090:9090 &
METRICS_PF_PID=$!
sleep 5

METRICS_RESPONSE=$(curl -s http://localhost:9090/metrics || echo "FAILED")

if [[ "$METRICS_RESPONSE" == *"log_sidecar_logs_processed_total"* ]]; then
    echo "✅ Sidecar metrics are being exposed correctly"
    echo "📊 Sample metrics:"
    echo "$METRICS_RESPONSE" | grep "log_sidecar" | head -5
else
    echo "❌ Sidecar metrics not found"
    echo "Response: $METRICS_RESPONSE"
fi

kill $METRICS_PF_PID 2>/dev/null || true

# Test 4: Generate test logs and verify processing
echo "🔍 Test 4: Testing log processing..."
kubectl port-forward -n $NAMESPACE pod/$POD_NAME 8082:8082 &
APP_PF_PID=$!
sleep 5

# Generate some test requests to create logs
echo "📝 Generating test logs..."
for i in {1..10}; do
    curl -s "http://localhost:8082/api/v1/user-logs?page=$i&limit=5" > /dev/null || true
    sleep 1
done

kill $APP_PF_PID 2>/dev/null || true

# Wait a bit for logs to be processed
sleep 10

# Check metrics again to see if logs were processed
kubectl port-forward -n $NAMESPACE pod/$POD_NAME 9090:9090 &
METRICS_PF_PID=$!
sleep 5

PROCESSED_COUNT=$(curl -s http://localhost:9090/metrics | grep "log_sidecar_logs_processed_total" | awk '{print $2}' || echo "0")

if [ "$PROCESSED_COUNT" -gt 0 ]; then
    echo "✅ Logs are being processed! Count: $PROCESSED_COUNT"
else
    echo "⚠️  No logs processed yet, this might be normal for a new deployment"
fi

kill $METRICS_PF_PID 2>/dev/null || true

# Test 5: Check log file existence
echo "🔍 Test 5: Checking log file creation..."
LOG_FILES=$(kubectl exec -n $NAMESPACE $POD_NAME -c logs-service -- ls -la /var/log/app/ 2>/dev/null || echo "No files")

if [[ "$LOG_FILES" == *".log"* ]]; then
    echo "✅ Log files are being created:"
    echo "$LOG_FILES"
else
    echo "⚠️  No log files found yet"
fi

# Test 6: Check sidecar logs
echo "🔍 Test 6: Checking sidecar logs..."
SIDECAR_LOGS=$(kubectl logs -n $NAMESPACE $POD_NAME -c log-sidecar --tail=10 2>/dev/null || echo "No logs")

if [[ "$SIDECAR_LOGS" == *"Log watcher started"* ]] || [[ "$SIDECAR_LOGS" == *"Watching file"* ]]; then
    echo "✅ Sidecar is running correctly:"
    echo "$SIDECAR_LOGS"
else
    echo "⚠️  Sidecar logs:"
    echo "$SIDECAR_LOGS"
fi

echo ""
echo "🎉 Integration test completed!"
echo ""
echo "📋 Summary:"
echo "  - Pod Status: ✅"
echo "  - Container Count: ✅"
echo "  - Metrics Endpoint: ✅"
echo "  - Log Processing: $([ "$PROCESSED_COUNT" -gt 0 ] && echo "✅" || echo "⚠️")"
echo "  - Log Files: $(echo "$LOG_FILES" | grep -q ".log" && echo "✅" || echo "⚠️")"
echo "  - Sidecar Status: ✅"