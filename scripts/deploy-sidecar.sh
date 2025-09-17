#!/bin/bash

set -e

# Configuration
REGISTRY=${REGISTRY:-"345215350058.dkr.ecr.ap-southeast-1.amazonaws.com/cs301-crm"}
NAMESPACE=${NAMESPACE:-"default"}
SIDECAR_VERSION=${SIDECAR_VERSION:-"latest"}

echo "Registry: $REGISTRY"
echo "Namespace: $NAMESPACE"
echo "Version: $SIDECAR_VERSION"

# Build and push sidecar image
echo "📦 Building sidecar Docker image..."
cd log-sidecar
docker build -t $REGISTRY/log-sidecar:$SIDECAR_VERSION .

echo "🔄 Pushing sidecar image to registry..."
docker push $REGISTRY/log-sidecar:$SIDECAR_VERSION

# Update Helm values
echo "⚙️  Updating Helm values..."
cd ../k8s/charts/logs-service

cat > sidecar-values.yaml << EOF
sidecar:
  enabled: true
  image:
    repository: $REGISTRY/log-sidecar
    tag: $SIDECAR_VERSION
    pullPolicy: Always
  
  loki:
    endpoint: "http://loki:3100"
  
  batchSize: 100
  flushIntervalMs: 5000

monitoring:
  enabled: true
EOF

# Deploy or upgrade the Helm chart
echo "🎯 Deploying logs-service with sidecar..."
helm upgrade --install logs-service . \
  --namespace $NAMESPACE \
  --values values.yaml \
  --values sidecar-values.yaml \
  --wait \
  --timeout 300s

# Verify deployment
echo "✅ Verifying deployment..."
kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=logs-service

echo "🔍 Checking sidecar metrics endpoint..."
kubectl port-forward -n $NAMESPACE svc/logs-service-sidecar 9090:9090 &
PF_PID=$!
sleep 5

if curl -s http://localhost:9090/metrics > /dev/null; then
    echo "✅ Sidecar metrics endpoint is accessible"
else
    echo "❌ Sidecar metrics endpoint is not accessible"
fi

kill $PF_PID 2>/dev/null || true

echo "🎉 Deployment completed successfully!"
echo ""
echo "📊 Access points:"
echo "  - Grafana: kubectl port-forward svc/grafana 3000:3000"
echo "  - Loki: kubectl port-forward svc/loki 3100:3100"
echo "  - Sidecar Metrics: kubectl port-forward svc/logs-service-sidecar 9090:9090"