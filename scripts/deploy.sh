#!/bin/bash

# Deployment script

set -e

ENVIRONMENT=$1

if [ -z "$ENVIRONMENT" ]; then
  echo "Environment not specified"
  exit 1
fi

helm upgrade --install myapp ./helm-chart --namespace $ENVIRONMENT --set image.tag=$(git rev-parse HEAD)
