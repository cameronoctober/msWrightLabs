# Lesson Plan Marketplace

A comprehensive marketplace platform for educators to buy and sell lesson plans and teaching resources. Built with Spring Boot and deployed as a modular monolith.

## Features

### For Buyers
- Browse and search thousands of teaching resources
- Advanced filtering by grade, subject, resource type, and price
- Secure payment processing via PayFast
- Instant digital delivery after purchase
- Personal library for managing purchases
- Wishlist and favorites
- Product reviews and ratings

### For Sellers
- Create and manage product listings
- Upload digital files of any format
- Set pricing and licensing options
- Track sales and revenue analytics
- Receive automated payouts
- Respond to customer reviews
- View customer insights

### For Admins
- User and seller moderation
- Product content approval
- Review moderation
- Category and tag management
- Site-wide configuration
- Revenue and commission tracking
- Audit logging and dispute resolution

## Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17
- **Template Engine**: Thymeleaf with Bootstrap 5
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Storage**: S3-compatible (AWS S3 or Minio)
- **Payment Gateway**: PayFast (South African payment processor)
- **Security**: Spring Security with role-based access control
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Project Structure

```
marketplace/
├── domain/                # JPA entities and repositories
├── auth-service/          # Authentication and user management
├── catalog-service/       # Product catalog and search
├── payment-service/       # Shopping cart, checkout, PayFast integration
├── storage-service/       # S3 file storage
├── admin-service/         # Admin tools and moderation
├── web-app/              # Main Spring Boot app with Thymeleaf controllers
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for local development)
- PostgreSQL 15 (if running locally without Docker)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourorg/lesson-marketplace.git
cd lesson-marketplace
```

### 2. Run with Docker Compose

The easiest way to get started is using Docker Compose, which will start PostgreSQL, Minio (S3-compatible storage), and the application:

```bash
docker-compose up --build
```

The application will be available at: **http://localhost:8080**

- **Minio Console**: http://localhost:9001 (minioadmin / minioadmin)
- **Database**: localhost:5432 (postgres / postgres)

### 3. Run Locally (Without Docker)

If you prefer to run the application locally:

#### Set up PostgreSQL

```bash
# Create database
createdb marketplace

# Or using psql
psql -U postgres -c "CREATE DATABASE marketplace;"
```

#### Configure Environment Variables

Create a `.env` file or export these variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=marketplace
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

export STORAGE_TYPE=s3
export S3_BUCKET=marketplace-files
export S3_REGION=us-east-1
export S3_ACCESS_KEY=your-access-key
export S3_SECRET_KEY=your-secret-key

export PAYFAST_MERCHANT_ID=10000100
export PAYFAST_MERCHANT_KEY=46f0cd694581a
export PAYFAST_PASSPHRASE=jt7NOE43FZPn
export PAYFAST_SANDBOX=true

export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
```

#### Build and Run

```bash
# Build the project
./mvnw clean package

# Run the application
java -jar web-app/target/web-app-1.0.0-SNAPSHOT.jar
```

## Configuration

All configuration is in `web-app/src/main/resources/application.yml`. Key configuration areas:

### Database
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### PayFast Payment Gateway
```yaml
payfast:
  merchant-id: ${PAYFAST_MERCHANT_ID}
  merchant-key: ${PAYFAST_MERCHANT_KEY}
  passphrase: ${PAYFAST_PASSPHRASE}
  sandbox-mode: ${PAYFAST_SANDBOX:true}
```

### Platform Commission
```yaml
platform:
  commission-rate: 0.15  # 15% platform fee on sales
```

## Testing

```bash
# Run all tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl catalog-service
```

## API Endpoints

### Public Endpoints
- `GET /` - Homepage
- `GET /products` - Product listing
- `GET /products/{id}` - Product details
- `GET /search` - Search products
- `POST /register` - User registration
- `POST /login` - User login

### Authenticated Endpoints (Buyer)
- `GET /cart` - View shopping cart
- `POST /cart/add` - Add to cart
- `POST /checkout` - Initiate checkout
- `GET /library` - My purchased resources
- `POST /reviews` - Leave a review

### Seller Endpoints (SELLER role)
- `GET /seller/dashboard` - Analytics dashboard
- `GET /seller/products` - Manage products
- `POST /seller/products` - Create product
- `PUT /seller/products/{id}` - Update product
- `POST /seller/products/{id}/files` - Upload files

### Admin Endpoints (ADMIN role)
- `GET /admin` - Admin dashboard
- `GET /admin/users` - User management
- `GET /admin/products` - Product moderation
- `GET /admin/reviews` - Review moderation

## PayFast Integration

This application uses PayFast for payment processing. PayFast is a popular South African payment gateway.

### Sandbox Mode

For testing, use PayFast sandbox credentials:
- Merchant ID: `10000100`
- Merchant Key: `46f0cd694581a`
- Passphrase: `jt7NOE43FZPn`

### IPN (Instant Payment Notification)

The webhook endpoint is: `POST /webhook/payfast`

PayFast will send payment notifications to this endpoint, which are validated using MD5 signature verification.

## Database Schema

The application uses Flyway for database migrations. The initial schema (`V1__initial_schema.sql`) creates:

- **users** - User accounts with role-based access
- **seller_profiles** - Seller information and payout details
- **products** - Lesson plans and resources
- **product_files** - Digital files attached to products
- **orders** - Purchase transactions
- **order_items** - Line items with revenue split
- **cart** / **cart_items** - Shopping cart
- **reviews** - Product reviews and ratings
- **payouts** - Seller payment tracking
- **downloads** - File download audit trail
- **audit_log** - System activity logging

## Security

### Authentication
- Form-based login with remember-me functionality
- Password hashing using BCrypt
- Email verification for new accounts
- Password reset via email token

### Authorization
- Role-based access control (BUYER, SELLER, ADMIN)
- Method-level security with `@PreAuthorize`
- CSRF protection enabled for all forms

### File Security
- Signed URLs with expiration for file downloads
- Purchase verification before granting download access
- Audit logging of all download events

## Deployment

### Production Checklist

1. **Update application.yml**:
   - Set `spring.jpa.hibernate.ddl-auto` to `validate`
   - Disable `spring.jpa.show-sql`
   - Configure production database connection
   - Set strong JWT secret
   - Configure real SMTP credentials

2. **PayFast Production**:
   - Update with real merchant credentials
   - Set `payfast.sandbox-mode: false`
   - Configure production return/cancel/notify URLs

3. **Storage**:
   - Use production S3 bucket with proper IAM policies
   - Enable S3 bucket encryption
   - Configure CDN (CloudFront) for file delivery

4. **Security**:
   - Use HTTPS everywhere
   - Configure proper CORS policies
   - Set up SSL certificates
   - Enable rate limiting

### Docker Deployment

```bash
# Build production image
docker build -t lesson-marketplace:latest .

# Run with environment variables
docker run -d \
  --name marketplace \
  -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PASSWORD=your-db-password \
  -e PAYFAST_MERCHANT_ID=your-merchant-id \
  lesson-marketplace:latest
```

### Kubernetes

See `k8s/` directory for Kubernetes deployment manifests (future addition).

## Monitoring

Spring Boot Actuator endpoints are available at `/actuator`:

- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running
- Check connection string in environment variables
- Ensure database user has proper permissions

### File Upload Failures
- Check S3 credentials
- Verify bucket exists and has correct permissions
- Ensure `spring.servlet.multipart.max-file-size` is sufficient

### PayFast IPN Not Working
- Verify webhook URL is publicly accessible
- Check PayFast signature validation logic
- Review PayFast dashboard for delivery attempts

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@lessonmarketplace.com or open an issue on GitHub.

## Acknowledgments

- PayFast for payment processing
- Spring Boot team for the excellent framework
- Community contributors
