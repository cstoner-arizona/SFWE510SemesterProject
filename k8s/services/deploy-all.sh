#!/bin/bash

# Deploy all services to Kubernetes in the correct order
# This script deploys the Skate Spots microservices application to the skate-spots namespace

set -e

NAMESPACE="skate-spots"

echo "========================================="
echo "Deploying Skate Spots Microservices"
echo "Namespace: $NAMESPACE"
echo "========================================="

# Check if namespace exists, create if not
if ! kubectl get namespace $NAMESPACE >/dev/null 2>&1; then
  echo "Creating namespace: $NAMESPACE"
  kubectl create namespace $NAMESPACE
else
  echo "Namespace $NAMESPACE already exists"
fi

# Deploy in dependency order

# echo ""
# echo "[1/9] Deploying databases (PostgreSQL)..."
# kubectl apply -f databases.yaml
# echo "Waiting for databases to be ready..."
# kubectl wait --for=condition=ready pod -l app=spot-database -n $NAMESPACE --timeout=120s
# kubectl wait --for=condition=ready pod -l app=skater-database -n $NAMESPACE --timeout=120s
# kubectl wait --for=condition=ready pod -l app=session-database -n $NAMESPACE --timeout=120s
#
# echo ""
# echo "[2/9] Deploying Redis..."
# kubectl apply -f redis.yaml
# echo "Waiting for Redis to be ready..."
# kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=60s
#
echo ""
echo "[3/9] Deploying ConfigServer..."
kubectl apply -f configserver.yaml
echo "Waiting for ConfigServer to be ready..."
kubectl wait --for=condition=ready pod -l app=configserver -n $NAMESPACE --timeout=120s

echo ""
echo "[4/9] Deploying EurekaServer..."
kubectl apply -f eurekaserver.yaml
echo "Waiting for EurekaServer to be ready..."
kubectl wait --for=condition=ready pod -l app=eurekaserver -n $NAMESPACE --timeout=120s

echo ""
echo "[5/9] Deploying Keycloak..."
kubectl apply -f keycloak.yaml
echo "Waiting for Keycloak to be ready (this may take a while)..."
kubectl wait --for=condition=ready pod -l app=keycloak -n $NAMESPACE --timeout=180s

echo ""
echo "[6/9] Deploying Spot Service..."
kubectl apply -f spot-service.yaml

echo ""
echo "[7/9] Deploying Skater Service..."
kubectl apply -f skater-service.yaml

echo ""
echo "[8/9] Deploying Session Service..."
kubectl apply -f session-service.yaml

echo "Waiting for business services to be ready..."
sleep 30

echo ""
echo "[9/9] Deploying Gateway Server..."
kubectl apply -f gatewayserver.yaml
echo "Waiting for Gateway to be ready..."
kubectl wait --for=condition=ready pod -l app=gatewayserver -n $NAMESPACE --timeout=120s

echo ""
echo "========================================="
echo "Deployment Complete!"
echo "========================================="
echo ""
echo "Checking pod status..."
kubectl get pods -n $NAMESPACE

echo ""
echo "Getting service endpoints..."
kubectl get svc -n $NAMESPACE

echo ""
echo "========================================="
echo "To access the Gateway:"
echo "  kubectl get svc gatewayserver -n $NAMESPACE"
echo ""
echo "To access Keycloak admin:"
echo "  kubectl get svc keycloak -n $NAMESPACE"
echo "  Default credentials: admin/admin"
echo ""
echo "To view logs:"
echo "  kubectl logs -f deployment/<service-name> -n $NAMESPACE"
echo "========================================="
