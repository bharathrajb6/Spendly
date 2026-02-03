# User Service

The **User Service** is the identity provider for the Spendly ecosystem. It manages user accounts, authentication, and profile information.

## 🔑 Core Responsibilities
- **Authentication**: Secure login using JWT (JSON Web Tokens).
- **Registration**: Onboarding new users and initializing their default configurations.
- **Profile Management**: Updating user details (names, emails) and security settings (passwords, status).
- **Admin Operations**: User listing, role management (Promote/Demote), and account toggling (Active/Deactivate).

## 🛠️ Technology Stack
- **Spring Boot**: Core framework.
- **MySQL**: Relational storage for user entities.
- **Security**: Spring Security + JWT.
- **Redis**: Caching frequently accessed user details for performance.
- **WebClient**: Synchronous communication with other services.

## 📡 API Endpoints (via API Gateway)
- `POST /api/v1/users/register`: Create a new account.
- `POST /api/v1/users/login`: Authenticate and receive a JWT.
- `GET /api/v1/users/me`: Retrieve currently logged-in user details.
- `PUT /api/v1/users/update`: Update profile information.
- `POST /api/v1/users/update-password`: Change user password.
- `GET /api/v1/admin/users`: (Admin Only) List all system users.

## ⚙️ Configuration
The service relies on the Following Environment Variables (or `application.yml` properties):
- `SPRING_DATASOURCE_URL`: Connectivity to MySQL.
- `USER_SERVICE_SECRET_KEY`: Used for JWT signing.
- `SPRING_DATA_REDIS_HOST`: Connectivity to Redis caching.
