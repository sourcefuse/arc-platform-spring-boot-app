#!/bin/bash

# Script for deploying the application to Kubernetes using Helm
# Usage: ./deploy.sh <environment> <image_tag>

set -e

# Check if required arguments are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <environment> <image_tag>"
    echo "Example: $0 dev docker.io/username/sample-spring-boot-app:latest"
    exit 1
fi

ENVIRONMENT=$1
IMAGE_TAG=$2

# Validate environment
if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "staging" && "$ENVIRONMENT" != "prod" ]]; then
    echo "Error: Environment must be one of: dev, staging, prod"
    exit 1
fi

echo "Deploying to $ENVIRONMENT environment with image tag: $IMAGE_TAG"

# Set namespace based on environment
NAMESPACE=$ENVIRONMENT

# Check if namespace exists, create if it doesn't
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    echo "Creating namespace: $NAMESPACE"
    kubectl create namespace "$NAMESPACE"
fi

# Set values file based on environment
VALUES_FILE="./helm/sample-spring-boot-app/values-${ENVIRONMENT}.yaml"

# Check if values file exists
if [ ! -f "$VALUES_FILE" ]; then
    echo "Error: Values file not found: $VALUES_FILE"
    exit 1
fi

# Deploy using Helm
echo "Deploying with Helm..."
helm upgrade --install sample-spring-boot-app ./helm/sample-spring-boot-app \
    --namespace "$NAMESPACE" \
    --values "$VALUES_FILE" \
    --set image.tag="$(echo $IMAGE_TAG | cut -d ':' -f 2)" \
    --set image.repository="$(echo $IMAGE_TAG | cut -d ':' -f 1)" \
    --atomic \
    --timeout 5m

# Check deployment status
if [ $? -eq 0 ]; then
    echo "Deployment to $ENVIRONMENT successful!"
    
    # Get service URL if available
    if kubectl get service sample-spring-boot-app -n "$NAMESPACE" &> /dev/null; then
        SERVICE_IP=$(kubectl get service sample-spring-boot-app -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        if [ -n "$SERVICE_IP" ]; then
            echo "Application is accessible at: http://$SERVICE_IP"
        else
            echo "Service is being provisioned. IP address not yet available."
        fi
    fi
else
    echo "Deployment to $ENVIRONMENT failed!"
    
    # Get pods status for debugging
    echo "\nPod status:"
    kubectl get pods -n "$NAMESPACE" -l app=sample-spring-boot-app
    
    # Get recent events
    echo "\nRecent events:"
    kubectl get events -n "$NAMESPACE" --sort-by='.lastTimestamp' | tail -n 10
    
    exit 1
fi

# Setup monitoring if in production
if [ "$ENVIRONMENT" == "prod" ]; then
    echo "Setting up monitoring for production environment..."
    
    # Apply Prometheus ServiceMonitor if using Prometheus Operator
    kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: sample-spring-boot-app
  namespace: $NAMESPACE
spec:
  selector:
    matchLabels:
      app: sample-spring-boot-app
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 15s
EOF
    
    echo "Monitoring setup complete."
fi

# Implement rollback mechanism
rollback() {
    echo "Rolling back deployment..."
    helm rollback sample-spring-boot-app -n "$NAMESPACE"
    
    if [ $? -eq 0 ]; then
        echo "Rollback successful!"
    else
        echo "Rollback failed! Manual intervention required."
        exit 1
    fi
}

# Add hook for rollback if needed
trap rollback ERR

echo "Deployment script completed successfully."