# Deployment Guide

This guide describes how to deploy the Spring Boot Marketplace application.

## 1. Prerequisites

Since this is a Java Spring Boot application, it requires a JVM environment. Static site hosts like Vercel or Netlify are **not** suitable for the backend.

Recommended Platforms:
- **Render** (Has a free tier for Web Services)
- **Railway** (Trial available, very easy setup)
- **Fly.io**
- **Heroku** (Paid only now)

## 2. Environment Variables

You must configure the following environment variables in your deployment platform:

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>

# File Storage (S3 Compatible - AWS, MinIO, DigitalOcean Spaces, etc.)
STORAGE_TYPE=s3
STORAGE_S3_ENDPOINT=<https://s3.region.amazonaws.com or equivalent>
STORAGE_S3_REGION=<region>
STORAGE_S3_ACCESS_KEY=<access-key>
STORAGE_S3_SECRET_KEY=<secret-key>
STORAGE_S3_BUCKET=<bucket-name>

# PayFast Payment Gateway
PAYFAST_MERCHANT_ID=<merchant-id>
PAYFAST_MERCHANT_KEY=<merchant-key>
PAYFAST_PASSPHRASE=<passphrase>
# Set to 'https://sandbox.payfast.co.za' for testing, 'https://www.payfast.co.za' for prod
PAYFAST_URL=https://sandbox.payfast.co.za

# Email (SMTP)
SPRING_MAIL_HOST=<smtp-host>
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<username>
SPRING_MAIL_PASSWORD=<password>

# Application Base URL (for callbacks/redirects)
APP_BASE_URL=https://<your-app-name>.onrender.com
```

## 3. Docker Deployment (Recommended)

The project includes a `Dockerfile`. To deploy:

### Option A: Render.com
1. Connect your GitHub repository to Render.
2. Create a new **Web Service**.
3. Select "Docker" as the Runtime.
4. Add the Environment Variables listed above.
5. Render will automatically build the Docker image and deploy it.

### Option B: Railway.app
1. Connect GitHub repo.
2. Railway detects the Dockerfile automatically.
3. Add a **PostgreSQL** database service (Railway provides this easily).
4. Configure variables.
5. Deploy.

## 4. Local Build & Run

To run locally with Docker:

```bash
# 1. Build the JAR
mvn clean install -DskipTests

# 2. Build Docker image
docker build -t marketplace-app .

# 3. Run container (ensure you pass env vars or use a .env file)
docker run -p 8080:8080 --env-file .env marketplace-app
```

## 5. Database Migrations

Flyway is included and implies that database migrations will run automatically on application startup. Ensure your database user has permissions to create tables.
