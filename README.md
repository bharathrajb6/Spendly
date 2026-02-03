# Spendly - Personal Finance Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-9cf)](https://microservices.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spendly is a comprehensive personal finance management system built with a microservices architecture. It helps users track their income, expenses, and financial goals while providing insights into their spending habits through a premium, dynamic web interface.

## 🚀 Features

- **User Management**: Secure authentication and user profile management.
- **Transaction Tracking**: Record and categorize income and expenses with support for recurring payments.
- **Financial Goals**: Set and track savings goals with automatic progress allocation.
- **Budgeting**: Manage and monitor spending against specific category budgets.
- **Analytics & Reports**: Visualize spending patterns and generate downloadable financial reports.
- **Real-time Notifications**: Instant updates via WebSockets and email alerts for transaction activity.

## 🏗️ System Architecture

Spendly follows a decoupled microservices architecture:

1. **API Gateway** (Port 8080) - Single entry point and request routing.
2. **User Service** (Port 8081) - Manages user accounts and authentication (JWT).
3. **Transaction Service** (Port 8082) - Handles all financial records and budgets.
4. **Goal Service** (Port 8083) - Manages and tracks financial targets.
5. **Export Report Service** (Port 8084) - Generates financial summaries (CSV/PDF).
6. **Email Service** (Port 8085) - Processes email notifications.
7. **Notification Service** (Port 8086) - Pushes real-time alerts via WebSockets.

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.6
- **API Gateway**: Spring Cloud Gateway
- **Frontend**: React 19, Vite (Premium UX/UI)
- **Database**: 
  - **MySQL 8.0** (Primary data store)
  - **Redis** (Caching layer)
- **Message Broker**: Apache Kafka (Event-driven communication)
- **Authentication**: JWT (JSON Web Tokens)
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- MySQL 8.0+
- Redis 7.0+
- Kafka 3.0+

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/Spendly.git
cd Spendly
```

### 2. Set Up Environment Variables
Create `.env` files in each service directory (if not using defaults) with the required variables.

### 3. Run with Docker Compose (Recommended)
This starts the entire infrastructure and all application services:
```bash
docker-compose up -d --build
```

### 4. Manual Setup (Alternative)
1. **Start Core Infrastructure**:
   ```bash
   docker-compose up -d mysql redis kafka zookeeper
   ```
2. **Build the Project**:
   ```bash
   mvn clean install
   ```
3. **Start Services**: Run the `.jar` files or use your IDE to launch each service (Gateway first).

## 📚 API Documentation

Once the services are running, access the aggregated API documentation at:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🧪 Testing

Run tests for all microservices:
```bash
mvn test
```

## 🧰 Development

### Code Style
- Follow Google Java Style Guide.
- Use 4 spaces for indentation.
- Maximum line length: 120 characters.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.
