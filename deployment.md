# AWS EKS Deployment Guide - Skate Spots Application

This guide walks you through deploying the Skate Spots microservices application to AWS using Amazon EKS (Elastic Kubernetes Service).

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [AWS Account Setup](#2-aws-account-setup)
3. [Install Required Tools](#3-install-required-tools)
4. [AWS CLI Configuration](#4-aws-cli-configuration)
5. [Create Supporting Infrastructure](#5-create-supporting-infrastructure)
6. [Create EKS Cluster](#6-create-eks-cluster)
7. [Push Docker Images to ECR](#7-push-docker-images-to-ecr)
8. [Deploy to Kubernetes](#8-deploy-to-kubernetes)
9. [Testing the Deployment](#9-testing-the-deployment)
10. [Cleanup](#10-cleanup)

---

## 1. Prerequisites

Before starting, ensure you have:

- [ ] Java 21 and Maven installed locally
- [ ] Docker installed and running
- [ ] Git repository with latest code
- [ ] Basic understanding of Kubernetes concepts
- [ ] At least 2GB of free disk space

---

## 2. AWS Account Setup

### 2.1 Create an AWS Account

1. Go to [https://aws.amazon.com](https://aws.amazon.com)
2. Click **"Create an AWS Account"**
3. Enter your email address and choose an account name
4. Verify your email and set a password
5. Choose **"Personal"** or **"Professional"** account type
6. Enter payment information (required even with credits)
7. Verify your phone number
8. Select the **Basic Support (Free)** plan

### 2.2 Apply Education Credits

1. Go to [AWS Educate](https://aws.amazon.com/education/awseducate/) or use your school's credit redemption link
2. Follow the instructions to apply credits to your account
3. Verify credits are applied: **Billing Dashboard → Credits**

### 2.3 Set Up IAM User (Security Best Practice)

Don't use the root account for daily work. Create an IAM user:

1. Go to **IAM** in AWS Console
2. Click **Users → Create user**
3. Username: `skate-admin`
4. Check **"Provide user access to the AWS Management Console"**
5. Select **"I want to create an IAM user"**
6. Set a console password
7. Click **Next**
8. Select **"Attach policies directly"**
9. Attach these policies:
   - `AmazonEKSClusterPolicy`
   - `AmazonEKSServicePolicy`
   - `AmazonEC2ContainerRegistryFullAccess`
   - `AmazonRDSFullAccess`
   - `AmazonVPCFullAccess`
   - `AmazonElastiCacheFullAccess`
   - `IAMFullAccess`
   - `CloudWatchFullAccess`
   - `AmazonEC2FullAccess`
10. Click **Create user**
11. Save the sign-in URL and credentials securely

### 2.4 Create Access Keys for CLI

1. Go to **IAM → Users → skate-admin**
2. Click **Security credentials** tab
3. Under **Access keys**, click **Create access key**
4. Select **"Command Line Interface (CLI)"**
5. Acknowledge the recommendation and click **Next**
6. Click **Create access key**
7. **Download the .csv file** or copy both keys - you won't see the secret key again!

---

## 3. Install Required Tools

### 3.1 AWS CLI

**Linux:**
```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

**macOS:**
```bash
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /
```

**Windows:**
Download and run: https://awscli.amazonaws.com/AWSCLIV2.msi

Verify installation:
```bash
aws --version
```

### 3.2 kubectl (Kubernetes CLI)

**Linux:**
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
kubectl version --client
```

**macOS:**
```bash
brew install kubectl
```

**Windows:**
```powershell
choco install kubernetes-cli
```

### 3.3 eksctl (EKS Cluster Manager)

**Linux/macOS:**
```bash
curl -sL "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
eksctl version
```

**Windows:**
```powershell
choco install eksctl
```

### 3.4 Helm (Kubernetes Package Manager)

**Linux/macOS:**
```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

**Windows:**
```powershell
choco install kubernetes-helm
```

---

## 4. AWS CLI Configuration

### 4.1 Configure Default Profile

```bash
aws configure
```

Enter when prompted:
- **AWS Access Key ID:** (from step 2.4)
- **AWS Secret Access Key:** (from step 2.4)
- **Default region:** `us-east-1` (or your preferred region)
- **Default output format:** `json`

### 4.2 Verify Configuration

```bash
aws sts get-caller-identity
```

You should see your account ID and IAM user ARN.

---

## 5. Create Supporting Infrastructure

We'll create infrastructure that EKS will use:
1. RDS PostgreSQL databases
2. ElastiCache Redis
3. ECR repositories

### 5.1 Set Environment Variables

```bash
export AWS_REGION=us-east-1
export PROJECT_NAME=skate-spots
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
```

### 5.2 Create VPC for EKS (eksctl will handle this)

eksctl will create a VPC automatically, but if you want to use existing infrastructure:

```bash
# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=${PROJECT_NAME}-vpc}]" \
  --query 'Vpc.VpcId' --output text)

aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames

echo "VPC ID: $VPC_ID"
```

### 5.3 Create RDS PostgreSQL Databases

First, create a DB subnet group (using the subnets that eksctl will create, or create them manually):

```bash
# Wait for EKS cluster creation to get subnet IDs, or create them now
# We'll create databases after the EKS cluster is ready
```

### 5.4 Create ECR Repositories

```bash
# Create repositories for each service
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service; do
  aws ecr create-repository \
    --repository-name ${PROJECT_NAME}/${service} \
    --image-scanning-configuration scanOnPush=true \
    --region $AWS_REGION
done

echo "ECR repositories created!"
```

---

## 6. Create EKS Cluster

### 6.1 Create Cluster with eksctl

This single command creates the EKS cluster, VPC, subnets, security groups, and node groups:

```bash
eksctl create cluster \
  --name ${PROJECT_NAME}-cluster \
  --region $AWS_REGION \
  --version 1.28 \
  --nodegroup-name ${PROJECT_NAME}-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 2 \
  --nodes-max 4 \
  --managed \
  --with-oidc \
  --ssh-access \
  --ssh-public-key ~/.ssh/id_rsa.pub
```

**Note:** This takes 15-20 minutes. Get a coffee! ☕

**If you don't have an SSH key:**
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
```

Or create without SSH access:
```bash
eksctl create cluster \
  --name ${PROJECT_NAME}-cluster \
  --region $AWS_REGION \
  --version 1.28 \
  --nodegroup-name ${PROJECT_NAME}-nodes \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 2 \
  --nodes-max 4 \
  --managed
```

### 6.2 Verify Cluster

```bash
# Check cluster status
eksctl get cluster --region $AWS_REGION

# Verify kubectl is configured
kubectl get nodes

# You should see 2 nodes in Ready state
```

### 6.3 Get VPC and Subnet Information

```bash
# Get VPC ID created by eksctl
VPC_ID=$(aws eks describe-cluster \
  --name ${PROJECT_NAME}-cluster \
  --region $AWS_REGION \
  --query 'cluster.resourcesVpcConfig.vpcId' \
  --output text)

# Get subnet IDs
SUBNET_IDS=$(aws eks describe-cluster \
  --name ${PROJECT_NAME}-cluster \
  --region $AWS_REGION \
  --query 'cluster.resourcesVpcConfig.subnetIds' \
  --output text)

echo "VPC ID: $VPC_ID"
echo "Subnet IDs: $SUBNET_IDS"
```

### 6.4 Create RDS Databases

Now that we have the VPC, create the databases:

```bash
# Get private subnet IDs (filter by tag or CIDR)
PRIVATE_SUBNETS=$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" "Name=tag:aws:cloudformation:logical-id,Values=SubnetPrivate*" \
  --query 'Subnets[*].SubnetId' \
  --output text)

# If the above doesn't work, manually get two subnets from different AZs:
SUBNET_1=$(echo $SUBNET_IDS | cut -d' ' -f1)
SUBNET_2=$(echo $SUBNET_IDS | cut -d' ' -f2)

# Create DB subnet group
aws rds create-db-subnet-group \
  --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
  --db-subnet-group-description "Subnet group for Skate Spots databases" \
  --subnet-ids $SUBNET_1 $SUBNET_2 \
  --region $AWS_REGION

# Get EKS cluster security group
CLUSTER_SG=$(aws eks describe-cluster \
  --name ${PROJECT_NAME}-cluster \
  --region $AWS_REGION \
  --query 'cluster.resourcesVpcConfig.clusterSecurityGroupId' \
  --output text)

# Create DB security group
DB_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-db-sg \
  --description "Security group for RDS" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query 'GroupId' --output text)

# Allow access from EKS cluster
aws ec2 authorize-security-group-ingress \
  --group-id $DB_SG \
  --protocol tcp \
  --port 5432 \
  --source-group $CLUSTER_SG \
  --region $AWS_REGION

# Create databases
for db in spot skater session; do
  aws rds create-db-instance \
    --db-instance-identifier ${PROJECT_NAME}-${db}-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 14 \
    --master-username postgres \
    --master-user-password ${db^}DbPassword123! \
    --allocated-storage 20 \
    --db-name ${db}_dev \
    --vpc-security-group-ids $DB_SG \
    --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
    --no-publicly-accessible \
    --backup-retention-period 0 \
    --region $AWS_REGION
done

echo "Databases are being created (takes 5-10 minutes)..."
```

Wait for databases to be ready:
```bash
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-spot-db --region $AWS_REGION
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-skater-db --region $AWS_REGION
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-session-db --region $AWS_REGION
```

Get database endpoints:
```bash
SPOT_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-spot-db \
  --region $AWS_REGION \
  --query 'DBInstances[0].Endpoint.Address' --output text)

SKATER_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-skater-db \
  --region $AWS_REGION \
  --query 'DBInstances[0].Endpoint.Address' --output text)

SESSION_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-session-db \
  --region $AWS_REGION \
  --query 'DBInstances[0].Endpoint.Address' --output text)

echo "Database endpoints:"
echo "SPOT_DB_HOST=$SPOT_DB_HOST"
echo "SKATER_DB_HOST=$SKATER_DB_HOST"
echo "SESSION_DB_HOST=$SESSION_DB_HOST"
```

### 6.5 Create ElastiCache Redis

```bash
# Create Redis security group
REDIS_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-redis-sg \
  --description "Security group for Redis" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress \
  --group-id $REDIS_SG \
  --protocol tcp \
  --port 6379 \
  --source-group $CLUSTER_SG \
  --region $AWS_REGION

# Create cache subnet group
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name ${PROJECT_NAME}-redis-subnet \
  --cache-subnet-group-description "Subnet group for Redis" \
  --subnet-ids $SUBNET_1 $SUBNET_2 \
  --region $AWS_REGION

# Create Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id ${PROJECT_NAME}-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --cache-subnet-group-name ${PROJECT_NAME}-redis-subnet \
  --security-group-ids $REDIS_SG \
  --region $AWS_REGION

echo "Redis cluster is being created..."
```

Get Redis endpoint:
```bash
# Wait for it to be available
sleep 300

REDIS_HOST=$(aws elasticache describe-cache-clusters \
  --cache-cluster-id ${PROJECT_NAME}-redis \
  --show-cache-node-info \
  --region $AWS_REGION \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)

echo "REDIS_HOST=$REDIS_HOST"
```

---

## 7. Push Docker Images to ECR

### 7.1 Authenticate Docker to ECR

```bash
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
```

### 7.2 Build and Push Images

From the project root directory:

```bash
# Build all services with Maven
mvn clean package -DskipTests

# Set ECR registry URL
ECR_REGISTRY=$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build and push Config Server
cd configserver
docker build --build-arg JAR_FILE=target/configserver-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/configserver:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/configserver:latest
cd ..

# Build and push Eureka Server
cd eurekaserver
docker build --build-arg JAR_FILE=target/eurekaserver-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/eurekaserver:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/eurekaserver:latest
cd ..

# Build and push Gateway Server
cd gatewayserver
docker build --build-arg JAR_FILE=target/gatewayserver-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/gatewayserver:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/gatewayserver:latest
cd ..

# Build and push Spot Service
cd spot-service
docker build --build-arg JAR_FILE=target/spot-service-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/spot-service:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/spot-service:latest
cd ..

# Build and push Skater Service
cd skater-service
docker build --build-arg JAR_FILE=target/skater-service-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/skater-service:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/skater-service:latest
cd ..

# Build and push Session Service
cd session-service
docker build --build-arg JAR_FILE=target/session-service-0.0.1-SNAPSHOT.jar \
  -t $ECR_REGISTRY/${PROJECT_NAME}/session-service:latest .
docker push $ECR_REGISTRY/${PROJECT_NAME}/session-service:latest
cd ..

echo "All images pushed to ECR!"
```

### 7.3 Pull and Push Keycloak Image

```bash
docker pull quay.io/keycloak/keycloak:23.0
docker tag quay.io/keycloak/keycloak:23.0 $ECR_REGISTRY/${PROJECT_NAME}/keycloak:23.0
aws ecr create-repository --repository-name ${PROJECT_NAME}/keycloak --region $AWS_REGION 2>/dev/null || true
docker push $ECR_REGISTRY/${PROJECT_NAME}/keycloak:23.0
```

---

## 8. Deploy to Kubernetes

Kubernetes will be used to deploy the services. The textbook should provide Kubernetes manifests, but I'll provide instructions for creating them if needed.

### 8.1 Create Kubernetes Namespace

```bash
kubectl create namespace skate-spots
kubectl config set-context --current --namespace=skate-spots
```

### 8.2 Create Secrets for Database Credentials

```bash
kubectl create secret generic db-credentials \
  --from-literal=spot-db-password='SpotDbPassword123!' \
  --from-literal=skater-db-password='SkaterDbPassword123!' \
  --from-literal=session-db-password='SessionDbPassword123!' \
  -n skate-spots
```

### 8.3 Create ConfigMap for Database Endpoints

```bash
kubectl create configmap db-endpoints \
  --from-literal=SPOT_DB_HOST=$SPOT_DB_HOST \
  --from-literal=SKATER_DB_HOST=$SKATER_DB_HOST \
  --from-literal=SESSION_DB_HOST=$SESSION_DB_HOST \
  --from-literal=REDIS_HOST=$REDIS_HOST \
  -n skate-spots
```

### 8.4 Deploy Observability Stack (ELK + Zipkin)

Your application uses the ELK stack (Elasticsearch, Logstash, Kibana) for logging and Zipkin for distributed tracing. Deploy these using Helm:

#### 8.4.1 Add Helm Repositories

```bash
# Add Elastic helm repo for ELK stack
helm repo add elastic https://helm.elastic.co

# Add OpenZipkin repo
helm repo add openzipkin https://openzipkin.github.io/zipkin

# Update repos
helm repo update
```

#### 8.4.2 Deploy Elasticsearch

```bash
# Deploy Elasticsearch (single node for dev/test)
helm install elasticsearch elastic/elasticsearch \
  --namespace skate-spots \
  --set replicas=1 \
  --set minimumMasterNodes=1 \
  --set resources.requests.cpu="500m" \
  --set resources.requests.memory="1Gi" \
  --set resources.limits.cpu="1000m" \
  --set resources.limits.memory="2Gi"

# Wait for Elasticsearch to be ready (takes 2-3 minutes)
kubectl wait --for=condition=ready pod -l app=elasticsearch-master -n skate-spots --timeout=300s

# Verify Elasticsearch is accessible
kubectl run curl-test --image=curlimages/curl -i --rm --restart=Never -n skate-spots -- \
  curl -s http://elasticsearch-master:9200
```

#### 8.4.3 Deploy Logstash

```bash
# Create Logstash configuration ConfigMap
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: logstash-config
  namespace: skate-spots
data:
  logstash.conf: |
    input {
      tcp {
        port => 5000
        codec => json
      }
    }

    filter {
      # Add any custom filters here
      mutate {
        add_field => { "[@metadata][target_index]" => "logs-%{service_name}-%{+YYYY.MM.dd}" }
      }
    }

    output {
      elasticsearch {
        hosts => ["elasticsearch-master:9200"]
        index => "%{[@metadata][target_index]}"
      }
      stdout {
        codec => rubydebug
      }
    }
EOF

# Deploy Logstash
helm install logstash elastic/logstash \
  --namespace skate-spots \
  --set replicas=1 \
  --set service.type=ClusterIP \
  --set service.ports[0].name=tcp \
  --set service.ports[0].port=5000 \
  --set service.ports[0].protocol=TCP \
  --set logstashConfig."logstash\.yml"="http.host: 0.0.0.0" \
  --set logstashPipeline."logstash\.conf"="$(cat <<'HEREDOC'
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  mutate {
    add_field => { "[@metadata][target_index]" => "logs-%{service_name}-%{+YYYY.MM.dd}" }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch-master:9200"]
    index => "%{[@metadata][target_index]}"
  }
  stdout {
    codec => rubydebug
  }
}
HEREDOC
)"

# Verify Logstash is running
kubectl get pods -l app=logstash-logstash -n skate-spots
```

#### 8.4.4 Deploy Kibana

```bash
# Deploy Kibana
helm install kibana elastic/kibana \
  --namespace skate-spots \
  --set replicas=1 \
  --set service.type=LoadBalancer \
  --set resources.requests.cpu="500m" \
  --set resources.requests.memory="1Gi"

# Wait for Kibana to be ready
kubectl wait --for=condition=ready pod -l app=kibana -n skate-spots --timeout=300s

# Get Kibana URL
KIBANA_URL=$(kubectl get svc kibana-kibana -n skate-spots \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Kibana URL: http://$KIBANA_URL:5601"
```

#### 8.4.5 Deploy Zipkin

```bash
# Deploy Zipkin for distributed tracing
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zipkin
  namespace: skate-spots
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zipkin
  template:
    metadata:
      labels:
        app: zipkin
    spec:
      containers:
      - name: zipkin
        image: openzipkin/zipkin:latest
        ports:
        - containerPort: 9411
        env:
        - name: STORAGE_TYPE
          value: "elasticsearch"
        - name: ES_HOSTS
          value: "http://elasticsearch-master:9200"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: zipkin
  namespace: skate-spots
spec:
  type: LoadBalancer
  selector:
    app: zipkin
  ports:
  - port: 9411
    targetPort: 9411
EOF

# Wait for Zipkin to be ready
kubectl wait --for=condition=ready pod -l app=zipkin -n skate-spots --timeout=120s

# Get Zipkin URL
ZIPKIN_URL=$(kubectl get svc zipkin -n skate-spots \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Zipkin URL: http://$ZIPKIN_URL:9411"
```

#### 8.4.6 Verify Observability Stack

```bash
# Check all pods are running
kubectl get pods -n skate-spots | grep -E "elasticsearch|logstash|kibana|zipkin"

# You should see:
# - elasticsearch-master-0 (Running)
# - logstash-logstash-0 (Running)
# - kibana-kibana-xxxxx (Running)
# - zipkin-xxxxx (Running)

# Test connectivity from within the cluster
kubectl run test-connectivity --image=curlimages/curl -i --rm --restart=Never -n skate-spots -- sh -c "
  echo 'Testing Elasticsearch...'
  curl -s http://elasticsearch-master:9200
  echo -e '\n\nTesting Zipkin...'
  curl -s http://zipkin:9411/health
"
```

Your services are already configured to send:
- **Logs** → Logstash:5000 → Elasticsearch → view in Kibana
- **Traces** → Zipkin:9411 → stored in Elasticsearch → view in Zipkin UI

### 8.5 Deploy Application Services

The textbook should provide Kubernetes manifests (YAML files) for deployments and services. They typically look like:

**Example structure for a Kubernetes deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: configserver
spec:
  replicas: 1
  selector:
    matchLabels:
      app: configserver
  template:
    metadata:
      labels:
        app: configserver
    spec:
      containers:
      - name: configserver
        image: ACCOUNT_ID.dkr.ecr.REGION.amazonaws.com/skate-spots/configserver:latest
        ports:
        - containerPort: 8089
---
apiVersion: v1
kind: Service
metadata:
  name: configserver
spec:
  selector:
    app: configserver
  ports:
  - port: 8089
    targetPort: 8089
```

**Apply the manifests from your textbook or create them based on the pattern above:**

```bash
# If you have manifests in k8s/ directory:
kubectl apply -f k8s/

# Or apply individually in order:
kubectl apply -f k8s/configserver.yaml
kubectl apply -f k8s/eurekaserver.yaml
kubectl apply -f k8s/keycloak.yaml
kubectl apply -f k8s/spot-service.yaml
kubectl apply -f k8s/skater-service.yaml
kubectl apply -f k8s/session-service.yaml
kubectl apply -f k8s/gatewayserver.yaml
```

### 8.6 Expose Gateway with LoadBalancer

```bash
kubectl expose deployment gatewayserver \
  --type=LoadBalancer \
  --name=gatewayserver-lb \
  --port=80 \
  --target-port=8072 \
  -n skate-spots
```

Wait for the LoadBalancer to be provisioned:
```bash
kubectl get svc gatewayserver-lb -n skate-spots -w
```

Get the external URL:
```bash
GATEWAY_URL=$(kubectl get svc gatewayserver-lb -n skate-spots \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Gateway URL: http://$GATEWAY_URL"
```

---

## 9. Testing the Deployment

### 9.1 Check Pod Status

```bash
kubectl get pods -n skate-spots
```

All pods should be in `Running` state.

### 9.2 View Logs

```bash
# View logs for a specific pod
kubectl logs -f deployment/gatewayserver -n skate-spots

# View logs for all pods
kubectl logs -l app=spot-service -n skate-spots
```

### 9.3 Test Health Endpoint

```bash
curl http://$GATEWAY_URL/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 9.4 Access Eureka Dashboard

```bash
curl http://$GATEWAY_URL/eureka
```

Or open in browser: `http://$GATEWAY_URL/eureka`

### 9.5 Test API Endpoints

Follow the same testing procedures as in your textbook. The service discovery and routing work automatically in Kubernetes!

### 9.6 Access Observability Dashboards

Your deployment includes full observability with Kibana (logs) and Zipkin (traces):

#### Access Kibana (View Logs)

```bash
# Get Kibana URL
KIBANA_URL=$(kubectl get svc kibana-kibana -n skate-spots \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Kibana: http://$KIBANA_URL:5601"
```

Open Kibana in your browser:
1. Go to **Stack Management → Index Patterns**
2. Create index pattern: `logs-*`
3. Go to **Discover** to view logs from all services
4. Filter by service: `service_name: "gatewayserver"` or `service_name: "spot-service"`

#### Access Zipkin (View Distributed Traces)

```bash
# Get Zipkin URL
ZIPKIN_URL=$(kubectl get svc zipkin -n skate-spots \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Zipkin: http://$ZIPKIN_URL:9411"
```

Open Zipkin in your browser:
1. Click **Run Query** to see recent traces
2. Click on any trace to see the full request flow through your microservices
3. Use filters to find specific services or operations

**This matches your local development setup!** All logs and traces flow to the same stack.

---

## 10. Cleanup

When you're done, clean up to avoid charges:

```bash
# Delete Kubernetes resources
kubectl delete namespace skate-spots

# Delete LoadBalancer service (if not deleted with namespace)
kubectl delete svc gatewayserver-lb -n skate-spots

# Delete EKS cluster (this also deletes VPC, subnets, etc.)
eksctl delete cluster --name ${PROJECT_NAME}-cluster --region $AWS_REGION

# Delete RDS instances
for db in spot skater session; do
  aws rds delete-db-instance \
    --db-instance-identifier ${PROJECT_NAME}-${db}-db \
    --skip-final-snapshot \
    --region $AWS_REGION
done

# Delete Redis cluster
aws elasticache delete-cache-cluster \
  --cache-cluster-id ${PROJECT_NAME}-redis \
  --region $AWS_REGION

# Delete ECR repositories
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service keycloak; do
  aws ecr delete-repository \
    --repository-name ${PROJECT_NAME}/${service} \
    --force \
    --region $AWS_REGION
done

echo "Cleanup complete!"
```

---

## Key Differences from ECS

### Why EKS is Better for Microservices:

1. **Automatic Service Discovery**
   - In Kubernetes, services can find each other by name automatically
   - `http://configserver:8089` works out of the box - no Service Connect needed!

2. **Standard Configuration**
   - Your Spring Boot configurations work without modification
   - No need for task definitions or ECS-specific setup

3. **Portability**
   - Kubernetes is cloud-agnostic
   - You can run the same manifests locally (Minikube) or on any cloud

4. **Industry Standard**
   - More documentation and community support
   - Skills transfer to other environments

5. **Better Scaling**
   - Horizontal Pod Autoscaler (HPA) built-in
   - More granular control over resources

---

## Troubleshooting

### Common Issues

**1. Pods stuck in Pending**
```bash
kubectl describe pod <pod-name> -n skate-spots
# Check for resource constraints or image pull errors
```

**2. ImagePullBackOff error**
```bash
# Verify ECR authentication
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Check image exists
aws ecr describe-images --repository-name ${PROJECT_NAME}/configserver --region $AWS_REGION
```

**3. Services can't connect to databases**
```bash
# Verify security group allows traffic from EKS nodes
# Check that database endpoints are correct in ConfigMap
kubectl describe configmap db-endpoints -n skate-spots
```

**4. Pod crashes or restarts**
```bash
kubectl logs <pod-name> -n skate-spots --previous
```

---

## Cost Optimization

1. **Use t3.small nodes** instead of t3.medium if workload allows
2. **Shut down cluster** when not in use (delete and recreate)
3. **Use Fargate for EKS** for serverless nodes (costs only when running)
4. **Set up auto-scaling** to scale down during low usage
5. **Monitor costs** with AWS Cost Explorer

---

## Next Steps

1. Refer to your textbook for Kubernetes manifest examples
2. Set up Ingress controller for better routing (NGINX or AWS Load Balancer Controller)
3. Implement Horizontal Pod Autoscaling
4. Set up monitoring with Prometheus and Grafana
5. Configure CI/CD with GitHub Actions

