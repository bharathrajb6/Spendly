# Spendly - Personal Finance Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-9cf)](https://microservices.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spendly is a comprehensive personal finance management system built with a microservices architecture. It helps users track their income, expenses, and financial goals while providing insights into their spending habits.

## 🚀 Features

- **User Management**: Secure authentication and user profile management
- **Transaction Tracking**: Record and categorize income and expenses
- **Financial Goals**: Set and track savings goals
- **Budgeting**: Manage and monitor spending against budgets
- **Analytics & Reports**: Visualize spending patterns and generate reports
- **Email Notifications**: Stay updated with transaction alerts and reports

## 🏗️ System Architecture

Spendly follows a microservices architecture with the following components:

1. **API Gateway** - Single entry point for all client requests
2. **User Service** - Manages user accounts and authentication
3. **Transaction Service** - Handles all financial transactions
4. **Goal Service** - Manages financial goals and savings targets
5. **Email Service** - Handles email notifications
6. **Export Report Service** - Generates and exports financial reports

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.6
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Spring Cloud Netflix Eureka
- **Database**: 
  - PostgreSQL (Primary data store)
  - Redis (Caching)
- **Message Broker**: Apache Kafka (Event-driven communication)
- **Authentication**: JWT (JSON Web Tokens)
- **Containerization**: Docker
- **Build Tool**: Maven

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL 14+
- Redis 7.0+
- Kafka 3.0+

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/Spendly.git
cd Spendly
```

### 2. Set Up Environment Variables
Create `.env` files in each service directory with the required environment variables.

### 3. Run with Docker Compose (Recommended)
This is the easiest way to run the entire application (Databases + Backend + Frontend).

```bash
# Build and start all services
docker-compose up -d --build
```
The following services will be available:
- **Spendly UI**: [http://localhost:5173](http://localhost:5173)
- **API Gateway**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Zipkin/Tracing**: (If enabled)

To stop the services:
```bash
docker-compose down
```

### 4. Manual Setup (Alternative)
If you prefer to run services manually using Maven:
1. Start Infrastructure:
   ```bash
   docker-compose up -d mysql kafka zookeeper
   ```
2. Build and Run Services:

## 📚 API Documentation

Once the services are running, access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🧪 Testing

Run tests for all services:
```bash
mvn test
```

## 🧰 Development

### Code Style
- Follow Google Java Style Guide
- Use 4 spaces for indentation
- Maximum line length: 120 characters

### Branching Strategy
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - Feature branches
- `bugfix/*` - Bug fixes

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Microservices.io](https://microservices.io)
- [Docker](https://www.docker.com/)
