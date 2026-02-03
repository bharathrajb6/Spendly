# Goal Service

The **Goal Service** empowers users to achieve their financial milestones by tracking progress toward custom-defined targets.

## 🎯 Core Responsibilities
- **Goal Management**: Create, update, and delete financial goals (e.g., "New Car", "Emergency Fund").
- **Real-time Progress Tracking**: Automatically recalculates progress percentages whenever new transactions occur.
- **Smart Allocation**: Distributes available savings proportionally across all active goals.
- **Milestone Notifications**: Triggers alerts when a goal is achieved or completed.

## 🛠️ Technology Stack
- **Spring Boot**: Core framework.
- **MySQL**: Persistent storage for goal entities.
- **Kafka**: Listens to the `transaction` topic for real-time recalibration.
- **Redis**: Caching goal details for rapid retrieval.

## 📡 API Endpoints (via API Gateway)
- `POST /api/v1/goal`: Define a new financial target.
- `GET /api/v1/goal/all/{userId}`: List all goals for a user.
- `PUT /api/v1/goal/{goalId}`: Modify goal targets or deadlines.
- `GET /api/v1/goal/{userId}/summary`: Aggregated stats on achieved vs. total goals.

## 🔔 Integration Flow
1. **Listen**: Consumes transaction events from the `transaction` Kafka topic.
2. **Calculate**: Computes the net impact on savings and updates all active goal percentages.
3. **Notify**: If a goal hits 100%, it publishes a message to `create-notification` and `send-email`.
