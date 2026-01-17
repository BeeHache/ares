# Ares

Ares is a robust RSS feed aggregator and podcast manager built with a modern tech stack. It features a secure backend API, a responsive frontend, and a containerized deployment strategy.

## 🚀 Tech Stack

### Backend
*   **Java 17+**
*   **Spring Boot 3.x**: Core framework
*   **Spring Security**: JWT-based authentication & RBAC
*   **Spring Data JPA**: Database interaction
*   **PostgreSQL**: Primary relational database
*   **Redis**: Caching and Refresh Token storage
*   **Thymeleaf**: Email templates
*   **Testcontainers**: Integration testing

### Frontend
*   **Angular**: Single Page Application (SPA)
*   **Node.js**: Server-Side Rendering (SSR)

### Infrastructure
*   **Docker & Docker Compose**: Containerization and orchestration
*   **Mailhog**: Email testing (Development)
*   **pgAdmin**: Database management UI

## 🛠️ Prerequisites

*   Java 17 SDK
*   Maven 3.8+
*   Node.js 18+ & npm
*   Docker & Docker Compose

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/BeeHache/ares.git
cd ares
```

### 2. Environment Configuration
Create a `.env` file in the root directory based on `.env.example` (if available) or set the following environment variables:

```properties
POSTGRES_DB=ares
POSTGRES_USER=ares
POSTGRES_PASSWORD=password
PGADMIN_EMAIL=admin@ares.com
PGADMIN_PASSWORD=password
JWT_SECRET=your_very_long_secret_key_here
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=user
SPRING_MAIL_PASSWORD=pass
```

### 3. Run with Docker Compose (Recommended)

**Development Environment:**
Starts Backend, Frontend, Postgres, Redis, Mailhog, and pgAdmin.
```bash
docker-compose -f dev-docker-compose.yml up --build -d
```

**Production Environment:**
Starts Backend, Frontend, Postgres, and Redis (no ports exposed for DBs).
```bash
docker-compose -f prod-docker-compose.yml up --build -d
```

### 4. Access the Application

*   **Frontend**: [http://localhost:4200](http://localhost:4200)
*   **Backend API**: [http://localhost:8080](http://localhost:8080)
*   **Mailhog (Dev)**: [http://localhost:8025](http://localhost:8025)
*   **pgAdmin**: [http://localhost:5050](http://localhost:5050)

## 🧪 Testing

Run unit and integration tests using Maven:

```bash
mvn test
```
*Note: Integration tests use Testcontainers and require Docker to be running.*

## 📂 Project Structure

```
ares/
├── src/main/java       # Spring Boot Backend Source
├── src/main/resources  # Config, SQL scripts, Templates
├── src/test            # Unit & Integration Tests
├── frontend/           # Angular Frontend Source
├── docker/             # Docker config files (if any)
├── dev-docker-compose.yml  # Dev orchestration
├── prod-docker-compose.yml # Prod orchestration
└── pom.xml             # Maven Build Configuration
```

## ✨ Key Features

*   **Secure Auth**: Full registration and login flow with JWT access tokens and Redis-backed refresh tokens.
*   **Feed Management**: Add, update, and delete RSS/Podcast feeds.
*   **OPML Support**: Import and export feed subscriptions via OPML files.
*   **Role Management**: Hierarchical role system (Admin, User, etc.).
*   **Email Notifications**: HTML email templates for account verification.
*   **Automated Updates**: Scheduled background tasks to fetch latest feed items.

## 🤝 Contributing

1.  Fork the repository
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
