# AWS Deployment Guide - Skate Spots Application

This guide walks you through deploying the Skate Spots microservices application to AWS using ECS Fargate.

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [AWS Account Setup](#2-aws-account-setup)
3. [Install Required Tools](#3-install-required-tools)
4. [AWS CLI Configuration](#4-aws-cli-configuration)
5. [Create AWS Infrastructure](#5-create-aws-infrastructure)
6. [Push Docker Images to ECR](#6-push-docker-images-to-ecr)
7. [Deploy Services to ECS](#7-deploy-services-to-ecs)
8. [Configure Networking & Load Balancer](#8-configure-networking--load-balancer)
9. [Testing the Deployment](#9-testing-the-deployment)
10. [Cleanup](#10-cleanup)

---

## 1. Prerequisites

Before starting, ensure you have:

- [ ] Java 21 and Maven installed locally
- [ ] Docker installed and running
- [ ] Git repository with latest code pushed
- [ ] Basic understanding of AWS services

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
6. Set a console password: %UUtB1+Q
7. Click **Next**
8. Select **"Attach policies directly"**
9. Attach these policies:
   - `AmazonECS_FullAccess`
   - `AmazonEC2ContainerRegistryFullAccess`
   - `AmazonRDSFullAccess`
   - `ElasticLoadBalancingFullAccess`
   - `AmazonVPCFullAccess`
   - `AmazonElastiCacheFullAccess`
   - `IAMFullAccess`
   - `CloudWatchFullAccess`
10. Click **Create user**
11. Save the sign-in URL and credentials securely
https://165787402199.signin.aws.amazon.com/console

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

### 3.2 ECS CLI (Optional but helpful)

```bash
sudo curl -Lo /usr/local/bin/ecs-cli https://amazon-ecs-cli.s3.amazonaws.com/ecs-cli-linux-amd64-latest
sudo chmod +x /usr/local/bin/ecs-cli
ecs-cli --version
```

### 3.3 Session Manager Plugin (for debugging)

**Linux:**
```bash
curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/ubuntu_64bit/session-manager-plugin.deb" -o "session-manager-plugin.deb"
sudo dpkg -i session-manager-plugin.deb
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

## 5. Create AWS Infrastructure

We'll create the infrastructure in this order:
1. VPC and Networking
2. RDS PostgreSQL databases
3. ElastiCache Redis
4. ECR repositories
5. ECS Cluster
6. Application Load Balancer

### 5.1 Set Environment Variables

```bash
export AWS_REGION=us-east-1
export PROJECT_NAME=skate-spots
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
```

### 5.2 Create VPC and Networking

```bash
# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=${PROJECT_NAME}-vpc}]" \
  --query 'Vpc.VpcId' --output text)

echo "VPC ID: $VPC_ID"

# Enable DNS hostnames
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications "ResourceType=internet-gateway,Tags=[{Key=Name,Value=${PROJECT_NAME}-igw}]" \
  --query 'InternetGateway.InternetGatewayId' --output text)

aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID

# Create public subnets (for load balancer)
PUBLIC_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone ${AWS_REGION}a \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-public-1}]" \
  --query 'Subnet.SubnetId' --output text)

PUBLIC_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone ${AWS_REGION}b \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-public-2}]" \
  --query 'Subnet.SubnetId' --output text)

# Enable auto-assign public IP
aws ec2 modify-subnet-attribute --subnet-id $PUBLIC_SUBNET_1 --map-public-ip-on-launch
aws ec2 modify-subnet-attribute --subnet-id $PUBLIC_SUBNET_2 --map-public-ip-on-launch

# Create private subnets (for services and databases)
PRIVATE_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.10.0/24 \
  --availability-zone ${AWS_REGION}a \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-private-1}]" \
  --query 'Subnet.SubnetId' --output text)

PRIVATE_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.11.0/24 \
  --availability-zone ${AWS_REGION}b \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-private-2}]" \
  --query 'Subnet.SubnetId' --output text)

# Create route table for public subnets
PUBLIC_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${PROJECT_NAME}-public-rt}]" \
  --query 'RouteTable.RouteTableId' --output text)

aws ec2 create-route --route-table-id $PUBLIC_RT --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID
aws ec2 associate-route-table --route-table-id $PUBLIC_RT --subnet-id $PUBLIC_SUBNET_1
aws ec2 associate-route-table --route-table-id $PUBLIC_RT --subnet-id $PUBLIC_SUBNET_2

# Create NAT Gateway for private subnets (allows outbound internet)
EIP_ALLOC=$(aws ec2 allocate-address --domain vpc --query 'AllocationId' --output text)

NAT_GW=$(aws ec2 create-nat-gateway \
  --subnet-id $PUBLIC_SUBNET_1 \
  --allocation-id $EIP_ALLOC \
  --tag-specifications "ResourceType=natgateway,Tags=[{Key=Name,Value=${PROJECT_NAME}-nat}]" \
  --query 'NatGateway.NatGatewayId' --output text)

echo "Waiting for NAT Gateway to become available..."
aws ec2 wait nat-gateway-available --nat-gateway-ids $NAT_GW

# Create route table for private subnets
PRIVATE_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${PROJECT_NAME}-private-rt}]" \
  --query 'RouteTable.RouteTableId' --output text)

aws ec2 create-route --route-table-id $PRIVATE_RT --destination-cidr-block 0.0.0.0/0 --nat-gateway-id $NAT_GW
aws ec2 associate-route-table --route-table-id $PRIVATE_RT --subnet-id $PRIVATE_SUBNET_1
aws ec2 associate-route-table --route-table-id $PRIVATE_RT --subnet-id $PRIVATE_SUBNET_2

echo "VPC Setup Complete!"
echo "VPC_ID=$VPC_ID"
echo "PUBLIC_SUBNET_1=$PUBLIC_SUBNET_1"
echo "PUBLIC_SUBNET_2=$PUBLIC_SUBNET_2"
echo "PRIVATE_SUBNET_1=$PRIVATE_SUBNET_1"
echo "PRIVATE_SUBNET_2=$PRIVATE_SUBNET_2"
```

**Save these IDs!** You'll need them for later steps.

### 5.3 Create Security Groups

```bash
# ALB Security Group (public access on 80/443)
ALB_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-alb-sg \
  --description "Security group for ALB" \
  --vpc-id $VPC_ID \
  --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 443 --cidr 0.0.0.0/0

# ECS Services Security Group
ECS_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-ecs-sg \
  --description "Security group for ECS services" \
  --vpc-id $VPC_ID \
  --query 'GroupId' --output text)

# Allow traffic from ALB to ECS services (ports 8070-8089)
aws ec2 authorize-security-group-ingress --group-id $ECS_SG --protocol tcp --port 8070-8089 --source-group $ALB_SG
# Allow internal communication between services
aws ec2 authorize-security-group-ingress --group-id $ECS_SG --protocol tcp --port 8070-8089 --source-group $ECS_SG

# Database Security Group
DB_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-db-sg \
  --description "Security group for RDS" \
  --vpc-id $VPC_ID \
  --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress --group-id $DB_SG --protocol tcp --port 5432 --source-group $ECS_SG

# Redis Security Group
REDIS_SG=$(aws ec2 create-security-group \
  --group-name ${PROJECT_NAME}-redis-sg \
  --description "Security group for ElastiCache Redis" \
  --vpc-id $VPC_ID \
  --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress --group-id $REDIS_SG --protocol tcp --port 6379 --source-group $ECS_SG

echo "Security Groups Created!"
echo "ALB_SG=$ALB_SG"
echo "ECS_SG=$ECS_SG"
echo "DB_SG=$DB_SG"
echo "REDIS_SG=$REDIS_SG"
```

### 5.4 Create RDS PostgreSQL Databases

```bash
# Create DB subnet group
aws rds create-db-subnet-group \
  --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
  --db-subnet-group-description "Subnet group for Skate Spots databases" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2

# Create Spot Database
aws rds create-db-instance \
  --db-instance-identifier ${PROJECT_NAME}-spot-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 14 \
  --master-username postgres \
  --master-user-password SpotDbPassword123! \
  --allocated-storage 20 \
  --db-name spot_dev \
  --vpc-security-group-ids $DB_SG \
  --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
  --no-publicly-accessible \
  --backup-retention-period 0

# Create Skater Database
aws rds create-db-instance \
  --db-instance-identifier ${PROJECT_NAME}-skater-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 14 \
  --master-username postgres \
  --master-user-password SkaterDbPassword123! \
  --allocated-storage 20 \
  --db-name skater_dev \
  --vpc-security-group-ids $DB_SG \
  --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
  --no-publicly-accessible \
  --backup-retention-period 0

# Create Session Database
aws rds create-db-instance \
  --db-instance-identifier ${PROJECT_NAME}-session-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 14 \
  --master-username postgres \
  --master-user-password SessionDbPassword123! \
  --allocated-storage 20 \
  --db-name session_dev \
  --vpc-security-group-ids $DB_SG \
  --db-subnet-group-name ${PROJECT_NAME}-db-subnet \
  --no-publicly-accessible \
  --backup-retention-period 0

echo "RDS instances are being created. This takes 5-10 minutes..."
echo "Check status with: aws rds describe-db-instances --query 'DBInstances[*].[DBInstanceIdentifier,DBInstanceStatus]'"
```

Wait for databases to be available:
```bash
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-spot-db
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-skater-db
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-session-db
```

Get database endpoints:
```bash
SPOT_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-spot-db \
  --query 'DBInstances[0].Endpoint.Address' --output text)

SKATER_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-skater-db \
  --query 'DBInstances[0].Endpoint.Address' --output text)

SESSION_DB_HOST=$(aws rds describe-db-instances \
  --db-instance-identifier ${PROJECT_NAME}-session-db \
  --query 'DBInstances[0].Endpoint.Address' --output text)

echo "SPOT_DB_HOST=$SPOT_DB_HOST"
echo "SKATER_DB_HOST=$SKATER_DB_HOST"
echo "SESSION_DB_HOST=$SESSION_DB_HOST"
```

### 5.5 Create ElastiCache Redis

```bash
# Create cache subnet group
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name ${PROJECT_NAME}-redis-subnet \
  --cache-subnet-group-description "Subnet group for Redis" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2

# Create Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id ${PROJECT_NAME}-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --cache-subnet-group-name ${PROJECT_NAME}-redis-subnet \
  --security-group-ids $REDIS_SG

echo "Redis cluster is being created..."
```

Get Redis endpoint when ready:
```bash
REDIS_HOST=$(aws elasticache describe-cache-clusters \
  --cache-cluster-id ${PROJECT_NAME}-redis \
  --show-cache-node-info \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)

echo "REDIS_HOST=$REDIS_HOST"
```

### 5.6 Create ECR Repositories

```bash
# Create repositories for each service
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service keycloak; do
  aws ecr create-repository \
    --repository-name ${PROJECT_NAME}/${service} \
    --image-scanning-configuration scanOnPush=true
done

echo "ECR repositories created!"
```

### 5.7 Create ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name ${PROJECT_NAME}-cluster \
  --capacity-providers FARGATE FARGATE_SPOT \
  --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1

echo "ECS Cluster created!"
```

---

## 6. Push Docker Images to ECR

### 6.1 Authenticate Docker to ECR

```bash
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
```

### 6.2 Build and Push Images

From the project root directory:

```bash
# Build all services
mvn clean package -DskipTests

# Build Docker images
mvn dockerfile:build -DskipTests

# Tag and push each image
ECR_REGISTRY=$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Config Server
docker tag skate/configserver:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/configserver:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/configserver:latest

# Eureka Server
docker tag skate/eurekaserver:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/eurekaserver:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/eurekaserver:latest

# Gateway Server
docker tag skate/gatewayserver:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/gatewayserver:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/gatewayserver:latest

# Spot Service
docker tag skate/spot-service:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/spot-service:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/spot-service:latest

# Skater Service
docker tag skate/skater-service:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/skater-service:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/skater-service:latest

# Session Service
docker tag skate/session-service:0.0.1-SNAPSHOT $ECR_REGISTRY/${PROJECT_NAME}/session-service:latest
docker push $ECR_REGISTRY/${PROJECT_NAME}/session-service:latest

echo "All images pushed to ECR!"
```

### 6.3 Push Keycloak Image

```bash
docker pull quay.io/keycloak/keycloak:23.0
docker tag quay.io/keycloak/keycloak:23.0 $ECR_REGISTRY/${PROJECT_NAME}/keycloak:23.0
docker push $ECR_REGISTRY/${PROJECT_NAME}/keycloak:23.0
```

---

## 7. Deploy Services to ECS

### 7.1 Create IAM Roles for ECS

```bash
# Create ECS Task Execution Role
aws iam create-role \
  --role-name ${PROJECT_NAME}-ecs-execution-role \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

aws iam attach-role-policy \
  --role-name ${PROJECT_NAME}-ecs-execution-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Create ECS Task Role (for application permissions)
aws iam create-role \
  --role-name ${PROJECT_NAME}-ecs-task-role \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'
```

### 7.2 Create CloudWatch Log Groups

```bash
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service keycloak; do
  aws logs create-log-group --log-group-name /ecs/${PROJECT_NAME}/${service}
done
```

### 7.3 Create Task Definitions

Create a file `task-definitions/configserver.json`:

```json
{
  "family": "skate-spots-configserver",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::ACCOUNT_ID:role/skate-spots-ecs-execution-role",
  "taskRoleArn": "arn:aws:iam::ACCOUNT_ID:role/skate-spots-ecs-task-role",
  "containerDefinitions": [
    {
      "name": "configserver",
      "image": "ACCOUNT_ID.dkr.ecr.REGION.amazonaws.com/skate-spots/configserver:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8089,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "native"},
        {"name": "ENCRYPT_KEY", "value": "secretkey"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/skate-spots/configserver",
          "awslogs-region": "REGION",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8089/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

I'll provide a script to generate all task definitions with your actual values:

```bash
# Create task-definitions directory
mkdir -p task-definitions

# Generate task definitions (run this script)
cat > generate-task-defs.sh << 'SCRIPT'
#!/bin/bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=${AWS_REGION:-us-east-1}
PROJECT_NAME=skate-spots

# Replace placeholders in task definitions
for file in task-definitions/*.json; do
  sed -i "s/ACCOUNT_ID/$ACCOUNT_ID/g" $file
  sed -i "s/REGION/$REGION/g" $file
done
SCRIPT

chmod +x generate-task-defs.sh
```

### 7.4 Register Task Definitions

```bash
# Register each task definition
aws ecs register-task-definition --cli-input-json file://task-definitions/configserver.json
aws ecs register-task-definition --cli-input-json file://task-definitions/eurekaserver.json
aws ecs register-task-definition --cli-input-json file://task-definitions/keycloak.json
aws ecs register-task-definition --cli-input-json file://task-definitions/gatewayserver.json
aws ecs register-task-definition --cli-input-json file://task-definitions/spot-service.json
aws ecs register-task-definition --cli-input-json file://task-definitions/skater-service.json
aws ecs register-task-definition --cli-input-json file://task-definitions/session-service.json
```

### 7.5 Create ECS Services

Deploy services in order (respecting dependencies):

```bash
# 1. Config Server (no dependencies)
aws ecs create-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service-name configserver \
  --task-definition ${PROJECT_NAME}-configserver \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}" \
  --service-connect-configuration '{
    "enabled": true,
    "namespace": "skate-spots",
    "services": [{
      "portName": "configserver",
      "clientAliases": [{"port": 8089, "dnsName": "configserver"}]
    }]
  }'

# Wait for Config Server to be healthy before proceeding
echo "Waiting for Config Server to be healthy..."
sleep 120

# 2. Eureka Server
aws ecs create-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service-name eurekaserver \
  --task-definition ${PROJECT_NAME}-eurekaserver \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}"

# 3. Keycloak
aws ecs create-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service-name keycloak \
  --task-definition ${PROJECT_NAME}-keycloak \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}"

# Wait for infrastructure services
echo "Waiting for Eureka and Keycloak..."
sleep 120

# 4. Business Services (can be deployed in parallel)
for service in spot-service skater-service session-service; do
  aws ecs create-service \
    --cluster ${PROJECT_NAME}-cluster \
    --service-name $service \
    --task-definition ${PROJECT_NAME}-$service \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}"
done

# 5. Gateway Server (last, needs all services registered)
sleep 60
aws ecs create-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service-name gatewayserver \
  --task-definition ${PROJECT_NAME}-gatewayserver \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}"
```

---

## 8. Configure Networking & Load Balancer

### 8.1 Create Application Load Balancer

```bash
# Create ALB
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name ${PROJECT_NAME}-alb \
  --subnets $PUBLIC_SUBNET_1 $PUBLIC_SUBNET_2 \
  --security-groups $ALB_SG \
  --scheme internet-facing \
  --type application \
  --query 'LoadBalancers[0].LoadBalancerArn' --output text)

ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns $ALB_ARN \
  --query 'LoadBalancers[0].DNSName' --output text)

echo "ALB DNS: $ALB_DNS"

# Create Target Group for Gateway
GATEWAY_TG=$(aws elbv2 create-target-group \
  --name ${PROJECT_NAME}-gateway-tg \
  --protocol HTTP \
  --port 8072 \
  --vpc-id $VPC_ID \
  --target-type ip \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --query 'TargetGroups[0].TargetGroupArn' --output text)

# Create Listener
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$GATEWAY_TG
```

### 8.2 Update Gateway Service with Load Balancer

```bash
aws ecs update-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service gatewayserver \
  --load-balancers targetGroupArn=$GATEWAY_TG,containerName=gatewayserver,containerPort=8072
```

---

## 9. Testing the Deployment

### 9.1 Get the ALB DNS Name

```bash
ALB_DNS=$(aws elbv2 describe-load-balancers \
  --names ${PROJECT_NAME}-alb \
  --query 'LoadBalancers[0].DNSName' --output text)

echo "Your API is available at: http://$ALB_DNS"
```

### 9.2 Health Check

```bash
curl http://$ALB_DNS/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 9.3 Get OAuth Token from Keycloak

First, you need to expose Keycloak or use the internal endpoint. For testing, you can create a separate target group:

```bash
# Get a token (replace with your Keycloak endpoint)
TOKEN=$(curl -X POST "http://$ALB_DNS/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=skate-app" \
  -d "client_secret=skate-app-secret" \
  -d "username=tony_hawk" \
  -d "password=password" | jq -r '.access_token')

echo "Token: $TOKEN"
```

### 9.4 Test API Endpoints

```bash
# Get all spots
curl -H "Authorization: Bearer $TOKEN" http://$ALB_DNS/api/spots

# Get skater info
curl -H "Authorization: Bearer $TOKEN" http://$ALB_DNS/api/skater/me

# Get sessions
curl -H "Authorization: Bearer $TOKEN" http://$ALB_DNS/api/session
```

### 9.5 View Logs

```bash
# View logs for a specific service
aws logs tail /ecs/${PROJECT_NAME}/gatewayserver --follow

# View logs for all services
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service; do
  echo "=== $service ==="
  aws logs tail /ecs/${PROJECT_NAME}/$service --since 1h
done
```

### 9.6 Check Service Status

```bash
# List all services
aws ecs list-services --cluster ${PROJECT_NAME}-cluster

# Describe a specific service
aws ecs describe-services \
  --cluster ${PROJECT_NAME}-cluster \
  --services gatewayserver \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount}'
```

---

## 10. Cleanup

When you're done with the project, clean up to avoid charges:

```bash
# Delete ECS Services
for service in gatewayserver spot-service skater-service session-service keycloak eurekaserver configserver; do
  aws ecs update-service --cluster ${PROJECT_NAME}-cluster --service $service --desired-count 0
  aws ecs delete-service --cluster ${PROJECT_NAME}-cluster --service $service --force
done

# Delete ECS Cluster
aws ecs delete-cluster --cluster ${PROJECT_NAME}-cluster

# Delete Load Balancer
aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN
aws elbv2 delete-target-group --target-group-arn $GATEWAY_TG

# Delete RDS Instances (skip final snapshot for dev)
aws rds delete-db-instance --db-instance-identifier ${PROJECT_NAME}-spot-db --skip-final-snapshot
aws rds delete-db-instance --db-instance-identifier ${PROJECT_NAME}-skater-db --skip-final-snapshot
aws rds delete-db-instance --db-instance-identifier ${PROJECT_NAME}-session-db --skip-final-snapshot

# Delete ElastiCache
aws elasticache delete-cache-cluster --cache-cluster-id ${PROJECT_NAME}-redis

# Delete ECR Repositories
for service in configserver eurekaserver gatewayserver spot-service skater-service session-service keycloak; do
  aws ecr delete-repository --repository-name ${PROJECT_NAME}/${service} --force
done

# Delete NAT Gateway and release EIP
aws ec2 delete-nat-gateway --nat-gateway-id $NAT_GW
sleep 60
aws ec2 release-address --allocation-id $EIP_ALLOC

# Delete Security Groups
aws ec2 delete-security-group --group-id $REDIS_SG
aws ec2 delete-security-group --group-id $DB_SG
aws ec2 delete-security-group --group-id $ECS_SG
aws ec2 delete-security-group --group-id $ALB_SG

# Delete Subnets
aws ec2 delete-subnet --subnet-id $PRIVATE_SUBNET_2
aws ec2 delete-subnet --subnet-id $PRIVATE_SUBNET_1
aws ec2 delete-subnet --subnet-id $PUBLIC_SUBNET_2
aws ec2 delete-subnet --subnet-id $PUBLIC_SUBNET_1

# Detach and delete Internet Gateway
aws ec2 detach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID
aws ec2 delete-internet-gateway --internet-gateway-id $IGW_ID

# Delete Route Tables (non-main)
aws ec2 delete-route-table --route-table-id $PRIVATE_RT
aws ec2 delete-route-table --route-table-id $PUBLIC_RT

# Delete VPC
aws ec2 delete-vpc --vpc-id $VPC_ID

echo "Cleanup complete!"
```

---

## Troubleshooting

### Common Issues

**1. Services failing to start**
```bash
# Check task stopped reasons
aws ecs describe-tasks \
  --cluster ${PROJECT_NAME}-cluster \
  --tasks $(aws ecs list-tasks --cluster ${PROJECT_NAME}-cluster --service-name gatewayserver --query 'taskArns[0]' --output text)
```

**2. Health checks failing**
- Check CloudWatch logs for errors
- Verify security groups allow traffic
- Ensure database connections are configured correctly

**3. Services can't find each other**
- Verify Eureka is running and healthy
- Check that service names in configuration match ECS service names
- Review security group rules for internal communication

**4. Database connection refused**
- Verify RDS security group allows traffic from ECS security group
- Check database endpoint is correctly configured in environment variables
- Ensure database is in "available" state

### Useful Commands

```bash
# Get running task IPs
aws ecs describe-tasks \
  --cluster ${PROJECT_NAME}-cluster \
  --tasks $(aws ecs list-tasks --cluster ${PROJECT_NAME}-cluster --query 'taskArns' --output text) \
  --query 'tasks[*].{Service:group,IP:containers[0].networkInterfaces[0].privateIpv4Address}'

# Force new deployment
aws ecs update-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service gatewayserver \
  --force-new-deployment

# Scale a service
aws ecs update-service \
  --cluster ${PROJECT_NAME}-cluster \
  --service spot-service \
  --desired-count 2
```

---

## Cost Optimization Tips

1. **Use Fargate Spot** for non-critical services (up to 70% savings)
2. **Stop services** when not in use for demos
3. **Use smallest instance sizes** (t3.micro for RDS, cache.t3.micro for Redis)
4. **Delete NAT Gateway** when not needed (costs ~$32/month)
5. **Set up billing alerts** in AWS Budgets

---

## Architecture Diagram

```
                    ┌─────────────────────────────────────────────────────────────┐
                    │                         AWS Cloud                            │
                    │  ┌─────────────────────────────────────────────────────────┐│
                    │  │                    Public Subnets                        ││
Internet ──────────►│  │  ┌─────────────┐                                        ││
                    │  │  │     ALB     │                                        ││
                    │  │  └──────┬──────┘                                        ││
                    │  └─────────┼────────────────────────────────────────────────┘│
                    │            │                                                 │
                    │  ┌─────────▼────────────────────────────────────────────────┐│
                    │  │                   Private Subnets                        ││
                    │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   ││
                    │  │  │   Gateway    │  │   Keycloak   │  │    Eureka    │   ││
                    │  │  │    Server    │  │              │  │    Server    │   ││
                    │  │  └──────┬───────┘  └──────────────┘  └──────────────┘   ││
                    │  │         │                                                ││
                    │  │  ┌──────▼───────┬───────────────┬───────────────┐       ││
                    │  │  │              │               │               │       ││
                    │  │  │ Spot Service │Skater Service │Session Service│       ││
                    │  │  │              │               │               │       ││
                    │  │  └──────┬───────┴───────┬───────┴───────┬───────┘       ││
                    │  │         │               │               │               ││
                    │  │  ┌──────▼───────┐┌──────▼───────┐┌──────▼───────┐       ││
                    │  │  │  RDS Spot    ││ RDS Skater   ││ RDS Session  │       ││
                    │  │  │  PostgreSQL  ││ PostgreSQL   ││ PostgreSQL   │       ││
                    │  │  └──────────────┘└──────────────┘└──────┬───────┘       ││
                    │  │                                         │               ││
                    │  │                                  ┌──────▼───────┐       ││
                    │  │                                  │ ElastiCache  │       ││
                    │  │                                  │    Redis     │       ││
                    │  │                                  └──────────────┘       ││
                    │  └──────────────────────────────────────────────────────────┘│
                    └─────────────────────────────────────────────────────────────┘
```

---

## Next Steps

After successful deployment:

1. **Set up HTTPS** with AWS Certificate Manager
2. **Configure a custom domain** with Route 53
3. **Set up CI/CD** with GitHub Actions or AWS CodePipeline
4. **Enable auto-scaling** for services
5. **Set up monitoring dashboards** in CloudWatch
