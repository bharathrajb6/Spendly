# Transaction Service

The **Transaction Service** is the financial heart of Spendly. It processes all income and expenses, manages budgets, and tracks savings growth.

## 💰 Core Responsibilities
- **Transaction Processing**: Add, Update, Delete, and Retrieve categorized transactions.
- **Budgeting**: Define and track spending limits across various categories.
- **Savings Management**: Maintain the user's total savings balance based on financial activity.
- **Recurring Transactions**: Handle automated transactions for fixed intervals.
- **Event Production**: Emits data to the `transaction` Kafka topic for consumption by other services.

## 🛠️ Technology Stack
- **Spring Boot**: Core framework.
- **MySQL**: Relational storage for transactions, savings, and budgets.
- **Redis**: Caching transaction lists and analytics for fast dashboard rendering.
- **Kafka**: Event producer for real-time synchronization.

## 📡 API Endpoints (via API Gateway)
- `POST /api/v1/transaction/add`: Record a new financial entry.
- `GET /api/v1/transaction/all/{username}`: Retrieve transaction history.
- `GET /api/v1/analytics/users/{username}/summary`: Get high-level income vs. expense stats.
- `GET /api/v1/savings/{username}`: Inspect current savings balance.
- `PUT /api/v1/budget/update`: Set or modify monthly budget targets.

## 🔔 Event Driven Flow
When a transaction is successfully recorded:
1. The service calculates the balance impact.
2. It publishes a message to the `transaction` topic.
3. The **Goal Service** consumes this to update savings progress.
4. The **Notification Service** pushes a real-time update to the UI.
