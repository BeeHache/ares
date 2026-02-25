# Ares

Ares is a robust RSS feed aggregator and podcast manager built with a modern tech stack. It features a secure backend API, a responsive frontend, and a containerized deployment strategy.

## 🚀 Tech Stack

### Backend
*   **Java 17+**
*   **Spring Boot 4.x**: Core framework
*   **Spring Security**: JWT-based authentication & RBAC
*   **Spring Data JPA**: Database interaction
*   **PostgreSQL**: Primary relational database with Full-Text Search
*   **Redis**: Caching and Refresh Token storage
*   **Thymeleaf**: Email templates
*   **Testcontainers**: Integration testing

### Frontend
*   **Angular 17+**: Single Page Application (SPA) with Server-Side Rendering (SSR)
*   **Node.js**: Server-Side Rendering runtime

### Infrastructure
*   **Docker & Docker Compose**: Containerization and orchestration
*   **Nginx**: Reverse proxy and SSL termination
*   **Mailhog**: Email testing (Development)

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
#postgres
POSTGRES_DB=ares
POSTGRES_USER=ares
POSTGRES_PASSWORD=password

#backend
ACTIVE_PROFILE=(prod|dev)
ALLOWED_ORIGINS=https://your.web.address.com
JWT_SECRET=your_very_long_secret_key_here
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=user
SPRING_MAIL_PASSWORD=pass
```

### 3. SSL Certificates (Production Only)
For the production setup, you must provide SSL certificates for Nginx.
1.  Create a directory: `mkdir -p nginx/certs`
2.  Place your certificate (`server.crt`) and private key (`server.key`) in this directory.
    *   *For local testing, you can generate self-signed certs:*
        ```bash
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout nginx/certs/server.key -out nginx/certs/server.crt
        ```

### 4. Run with Docker Compose

**Development Environment:**
Starts Backend (8080), Frontend (4200), Postgres, Redis, and Mailhog (8025).
```bash
docker-compose -f dev-docker-compose.yml up --build -d
```

**Production Environment:**
Starts the full stack behind Nginx. Only ports 80 (HTTP -> HTTPS redirect) and 443 (HTTPS) are exposed.
```bash
docker-compose -f prod-docker-compose.yml up --build -d
```

### 5. Access the Application

**Development:**
*   **Frontend**: [http://localhost:4200](http://localhost:4200)
*   **Backend API**: [http://localhost:8080](http://localhost:8080)
*   **Mailhog**: [http://localhost:8025](http://localhost:8025)

**Production:**
*   **Application**: [https://localhost](https://localhost) (or your domain)
*   *Note: Direct access to backend/database ports is blocked for security.*

## 🧪 Testing

Run unit and integration tests using Maven:

```bash
cd backend
mvn test
```
*Note: Integration tests use Testcontainers and require Docker to be running.*

## 📂 Project Structure

```
ares/
├── backend/                # Spring Boot Backend
│   ├── src/main/java       # Source
│   ├── src/main/resources  # Config, SQL scripts, Templates
|   ├── src/test            # Unit & Integration Tests
│   └── pom.xml             # Maven Build Configuration
├── frontend/               # Angular Frontend Source
├── nginx/                  # Nginx config & Dockerfile
├── dev-docker-compose.yml  # Dev orchestration
├── prod-docker-compose.yml # Prod orchestration
└── README.md
```

## ✨ Key Features

*   **Secure Auth**: Full registration and login flow with JWT access tokens and Redis-backed refresh tokens.
*   **Feed Management**: Add, update, and delete RSS/Podcast feeds.
*   **Full-Text Search**: Fast search across all feed items using Postgres tsvector.
*   **OPML Support**: Import and export feed subscriptions via OPML files.
*   **Role Management**: Hierarchical role system (Admin, User, etc.).
*   **Email Notifications**: HTML email templates for account verification.
*   **Automated Updates**: Scheduled background tasks to fetch latest feed items.
*   **Responsive UI**: Mobile-friendly design with secure password inputs.

## 🤝 Contributing

1.  Fork the repository
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
