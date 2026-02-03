# Spendly - Personal Finance Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-9cf)](https://microservices.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spendly is a state-of-the-art personal finance management system built using a robust **Microservices Architecture**. It empowers users to take full control of their financial journey by tracking transactions, setting ambitious goals, and receiving real-time insights—all through a premium, dynamic web interface.

---

## 🏗️ System Architecture

Spendly is designed for scalability and resilience, leveraging asynchronous communication and dedicated services for each core domain.

### Service Ecosystem

| Service | Port | Base Path | Responsibility |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `8080` | `/` | Unified entry point, request routing, and aggregated Swagger docs. |
| **User Service** | `8081` | `/api/v1/users` | Handles authentication, registration, and profile management. |
| **Transaction Service** | `8082` | `/api/v1/transaction` | Processes income/expenses, recurring transactions, and savings updates. |
| **Goal Service** | `8083` | `/api/v1/goal` | Tracks financial targets and progress based on real-time transaction events. |
| **Export Report Service**| `8084` | `/api/v1/report` | Generates financial reports in various formats (CSV, PDF). |
| **Email Service** | `8085` | N/A | Dispatches email notifications for transaction alerts and reports (Event-driven). |
| **Notification Service** | `8086` | `/api/v1/notifications`| Real-time WebSocket notifications for goal achievements and system alerts. |
| **Spendly UI** | `5173` | `/` | Modern React-based frontend providing a seamless user experience. |

### 🛠️ Infrastructure & Messaging
- **Database**: 
  - **MySQL 8.0**: Shared relational store with isolated tables per service domain.
  - **Redis**: Caching layer for user sessions, budget data, and transaction summaries.
- **Message Broker**: **Apache Kafka** manages the event-driven backbone:
  - `transaction`: Emitted by Transaction Service, consumed by Goal Service for progress updates.
  - `send-email`: Consumed by Email Service to trigger user notifications.
  - `create-notification`: Consumed by Notification Service to push WebSocket alerts.
  - `REPORT-CSV` / `REPORT0-PDF`: Internal topics for report generation queuing.

---

## 🚀 Key Features

- **Dynamic Dashboard**: Real-time overview of current balance, recent activity, and goal progress.
- **Smart Transaction Management**: Categorized income and expense tracking with support for recurring payments.
- **Goal Allocator**: Sophisticated logic that automatically distributes savings across multiple financial goals.
- **Interactive Reports**: On-demand report generation with asynchronous delivery to your email.
- **Real-time Alerts**: Instant feedback via WebSockets for milestones and account changes.
- **Secure Profile Management**: Comprehensive settings to update personal details, security preferences, and notification toggles.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.5.6, Spring Cloud Gateway
- **Frontend**: React 19, Vite, Vanilla CSS (Premium Rich Aesthetics)
- **Database**: MySQL 8.0, Redis
- **Messaging**: Apache Kafka (Confluent Platform)
- **Security**: JWT-based Authentication, Spring Security
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

---

## 📦 Getting Started

### Prerequisites

- **Docker Desktop** (includes Docker Compose)
- **Java 17 & Maven 3.8+** (for manual builds)
- **Node.js 18+** (for frontend development)

### 🚀 Running with Docker Compose (Highly Recommended)

Spendly is fully containerized. You can launch the entire ecosystem with a single command:

```bash
# Clone the repository
git clone https://github.com/yourusername/Spendly.git
cd Spendly

# Start all services (Infrastructure + Backend + Frontend)
docker-compose up -d --build
```

#### Access Points:
- **Web App**: [http://localhost:5173](http://localhost:5173)
- **Unified API Gateway Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Database (MySQL)**: `localhost:3308` (Root: `Hunter@4343`)
- **Redis**: `localhost:6379`

### Manual Development Setup

1. Start the core infrastructure: `docker-compose up -d mysql redis kafka zookeeper`
2. Navigate to a service folder (e.g., `user-service`) and run: `mvn spring-boot:run`
3. Start the UI: `cd spendly-ui && npm install && npm run dev`

---

## 📚 Recent Enhancements

- **Profile Update Fix**: Resolved issues where partial profile updates would fail due to missing optional fields.
- **Comprehensive JavaDocs**: Generated detailed method-level documentation for all microservices.
- **Unified Logging**: Improved traceability across service boundaries using standardized logging formats.
- **Enhanced UI Persistence**: Fixed state management in the Settings page to handle account updates and password changes seamlessly.

---

## 🤝 Contributing

We welcome contributions! Please fork the project and submit a PR for any features or bug fixes.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
