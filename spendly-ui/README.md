# Spendly UI - Premium Financial Dashboard

This is the frontend application for the **Spendly** Personal Finance Management System. It provides a sleek, high-performance interface for managing your wealth.

## 🚀 Key Features

*   **Financial Overview**: Interactive charts and cards showing your net worth, income, and expenses.
*   **Transaction Hub**: A centralized place to view, add, and filter your financial history.
*   **Goal Tracking**: Visual progress bars and milestone tracking for your savings goals.
*   **Account Settings**: Securely update your profile, change your password, and manage your account status.
*   **Live Notifications**: Real-time feedback and system alerts delivered via WebSockets.

## 🛠️ Tech Stack

*   **React 19**: Leveraging the latest features for a reactive and performant UI.
*   **Vite**: Lightning-fast development and optimized production builds.
*   **Vanilla CSS**: Custom-crafted styles with a focus on premium aesthetics (Glassmorphism, Vibrant Gradients).
*   **State Management**: React Context & Hooks for efficient data flow.
*   **Communication**: Axios for RESTful APIs and STOMP for real-time WebSocket communication.

## 📦 Getting Started

### Prerequisites
*   **Node.js**: v18 or higher
*   **Local Backend**: The UI expects the Spendly Backend to be running on `http://localhost:8080`.

### Setup
1.  **Install Dependencies**:
    ```bash
    npm install
    ```
2.  **Run Development Server**:
    ```bash
    npm run dev
    ```
3.  **Build for Production**:
    ```bash
    npm run build
    ```

## 🏗️ Folder Structure
*   `src/api`: Centralized API calls for all microservices.
*   `src/components`: UI components organized by feature.
*   `src/context`: Global state providers (User, Theme, Notifications).
*   `src/pages`: Main application views (Dashboard, Transactions, Settings, etc.).
*   `src/styles`: Design tokens, global variables, and utility classes.

---
This UI is a core part of the larger **Spendly** Microservices Ecosystem. For information on the backend services and full system deployment, please refer to the [Root README](../README.md).
