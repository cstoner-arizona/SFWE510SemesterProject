# Kubernetes Deployments for Skate Spots Application

This directory contains Kubernetes deployment manifests for all microservices in the Skate Spots application.

## Services Included

### Infrastructure Services
1. **ConfigServer** (Port 8089) - Centralized configuration management
2. **EurekaServer** (Port 8070) - Service discovery and registration
3. **Keycloak** (Port 8080) - Authentication and authorization (OAuth2/JWT)
4. **Redis** (Port 6379) - Caching service for session-service

### Business Services
5. **Spot Service** (Port 8080) - Manages skate spot data
6. **Skater Service** (Port 8081) - Manages skater profiles
7. **Session Service** (Port 8082) - Manages skate sessions with Redis caching

### API Gateway
8. **Gateway Server** (Port 8072) - API Gateway with routing and security

### Databases
9. **PostgreSQL Databases** - Three separate databases for spot, skater, and session services

## Deployment Order

Services must be deployed in the following order to respect dependencies:

1. Databases (PostgreSQL instances)
2. Redis
3. ConfigServer (foundation service)
4. EurekaServer (depends on ConfigServer)
5. Keycloak (authentication service)
6. Business Services (Spot, Skater, Session)
7. Gateway Server (depends on all above)

## Quick Deployment

### Option 1: Use the deployment script (Recommended)

```bash
cd k8s/services
./deploy-all.sh
```

This script will:
- Create the namespace if it doesn't exist
- Deploy all services in the correct order
- Wait for each critical service to be ready before proceeding
- Display pod and service status at the end

### Option 2: Manual deployment

```bash
# Create namespace
kubectl create namespace skate-spots

# Deploy in order
kubectl apply -f databases.yaml
kubectl apply -f redis.yaml
kubectl apply -f configserver.yaml
kubectl apply -f eurekaserver.yaml
kubectl apply -f keycloak.yaml
kubectl apply -f spot-service.yaml
kubectl apply -f skater-service.yaml
kubectl apply -f session-service.yaml
kubectl apply -f gatewayserver.yaml
```

## Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n skate-spots

# Check services
kubectl get svc -n skate-spots

# View logs for a specific service
kubectl logs -f deployment/gatewayserver -n skate-spots
```

## Access Services

### Gateway Server (Main Entry Point)

```bash
# Get the LoadBalancer external URL
kubectl get svc gatewayserver -n skate-spots

# Access via:
# http://<EXTERNAL-IP>:8072
```

### Keycloak Admin Console

```bash
# Get the LoadBalancer external URL
kubectl get svc keycloak -n skate-spots

# Access via:
# http://<EXTERNAL-IP>:8080

# Default credentials:
# Username: admin
# Password: admin
```

### Eureka Dashboard

Access via Gateway:
```
http://<GATEWAY-IP>:8072/eureka/web
```

## Service Configuration

All services are configured with:

- **Resource Limits**:
  - Requests: 250m CPU, 512Mi memory
  - Limits: 500m CPU, 1Gi memory

- **Health Checks**:
  - Liveness and readiness probes on `/actuator/health`

- **Tracing**:
  - All services send traces to Zipkin at `http://zipkin:9411`

- **Service Discovery**:
  - All business services register with Eureka

## Environment Variables

Key environment variables used:

- `SPRING_PROFILES_ACTIVE=dev` - Activates dev profile
- `CONFIGSERVER_URI=http://configserver:8089` - Config server location
- `REDIS_HOST=redis` - Redis host for session-service
- `KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/skate` - Keycloak realm

## Database Connections

Each service connects to its own PostgreSQL database:

- **Spot Service**: `spot-database:5432/spot_dev`
- **Skater Service**: `skater-database:5432/skater_dev`
- **Session Service**: `session-database:5432/session_dev`

Default credentials: `postgres/postgres`

**Note**: For production, use AWS RDS instead of in-cluster PostgreSQL. Update the database hostnames in the ConfigServer configuration files.

## Troubleshooting

### Pods not starting

```bash
# Check pod details
kubectl describe pod <pod-name> -n skate-spots

# Check logs
kubectl logs <pod-name> -n skate-spots

# For previous crashed instance
kubectl logs <pod-name> -n skate-spots --previous
```

### Services can't connect

```bash
# Test connectivity from a test pod
kubectl run test-pod --image=curlimages/curl -i --rm --restart=Never -n skate-spots -- \
  curl http://configserver:8089/actuator/health
```

### Reset deployment

```bash
# Delete all resources
kubectl delete namespace skate-spots

# Redeploy
kubectl create namespace skate-spots
./deploy-all.sh
```

## Files

- `configserver.yaml` - Config Server deployment
- `eurekaserver.yaml` - Eureka Server deployment
- `gatewayserver.yaml` - API Gateway deployment
- `keycloak.yaml` - Keycloak authentication server
- `redis.yaml` - Redis cache server
- `databases.yaml` - PostgreSQL databases for all services
- `spot-service.yaml` - Spot Service deployment
- `skater-service.yaml` - Skater Service deployment
- `session-service.yaml` - Session Service deployment
- `deploy-all.sh` - Automated deployment script
- `README.md` - This file

## Docker Images

All application images are pulled from ECR:
```
165787402199.dkr.ecr.us-east-1.amazonaws.com/skate-spots/<service-name>:latest
```

Before deploying, ensure:
1. Images have been built and pushed to ECR
2. EKS cluster has permissions to pull from ECR
3. Images are tagged as `latest` or update the image tags in manifests
