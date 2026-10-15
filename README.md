# START GYM — Wellness Navigator

A full-stack fitness and wellness web application for discovering workout programs, following exercise plans, tracking progress, and receiving personalized course recommendations.

## Overview

START GYM is a graduation project built with a React frontend and a Java Spring Boot backend. The two applications communicate through REST APIs, with MySQL used for persistent storage.

The platform combines workout/course management, user accounts, personalized recommendations, administration, and integrations with external services.

## Key Features

### Personalized course recommendation with a Decision Tree

Users can complete a multi-step questionnaire to receive a workout/course recommendation tailored to their profile. The questionnaire collects attributes including:

- Age and gender
- BMI
- Training goals
- Previous training experience
- Occupation and work environment

The backend uses a Decision Tree approach to map the submitted profile to a suitable course. The project’s recommendation logic uses entropy and information gain to select decision attributes; the selected course is then returned to the frontend. The result screen can display the recommendation together with user metrics and additional course information.

The recommendation flow is available to authenticated users from the **Recommend** page. The frontend submits questionnaire data to the backend and presents the returned course recommendation.

### User features

- Register, sign in, and manage account/profile information.
- Google OAuth2 sign-in integration.
- Browse workout collections and course details.
- View workout plans organized by course and day.
- Receive personalized course recommendations.
- Browse blog, recipe, about, and contact pages.
- Reset forgotten passwords.
- Access enrolled and recommended courses.
- Upgrade to VIP courses through VNPay sandbox integration.
- Receive email notifications through Gmail SMTP integration.

### Administration

- Dashboard with administrative sections for courses, exercises, customers, customer types, comments, and revenue.
- Create and edit courses and exercises.
- Manage customer records and customer types.
- View course details and customer course enrollments.

### External integrations

- **VNPay sandbox** for payment flow testing.
- **Google OAuth2** for sign-in.
- **Firebase / Google Cloud Storage** for application storage-related functionality.
- **Gmail SMTP** for sending email.
- **Anthropic Claude API** for AI-assisted consultation.

## Technology Stack

### Backend

- Java 8
- Spring Boot 2.7.13
- Spring Web and REST APIs
- Spring Security
- Spring Data JPA / Hibernate
- Spring Batch
- Spring OAuth2 Client and Resource Server
- JWT and BCrypt password encoding
- MySQL 8
- Gradle 8.2.1
- Lombok
- Springdoc OpenAPI / Swagger UI
- Firebase Admin SDK
- Google Cloud Storage and Google API Client
- Spring Mail
- Anthropic Claude API
- SLF4J / Logback

### Frontend

- React 18
- JavaScript
- React Router DOM
- Axios
- Material UI (MUI)
- Ant Design and Bootstrap
- Chart.js and Recharts
- Firebase SDK
- Google OAuth libraries
- React Player and React YouTube
- Yarn or npm

### Deployment

- Docker Compose for MySQL
- Caddy for static frontend hosting and API reverse proxy
- PM2 for managing the packaged Spring Boot application

## Architecture

```text
React Frontend (port 3000 during development)
        |
        | REST API / Axios
        v
Spring Boot Backend (port 8080)
        |
        +---- MySQL 8
        |
        +---- Google OAuth2 / Firebase / Cloud Storage
        +---- Gmail SMTP
        +---- VNPay sandbox
        +---- Anthropic Claude API
```

## Repository Structure

```text
DATN/
├── devops/
│   ├── caddyfile
│   ├── docker-compose.yml
│   └── ecosystem.config.js
├── start-gym-BE/
│   └── start-gym-BE/
│       └── wellness-navigator-gym/
│           ├── src/
│           ├── controller/
│           ├── build.gradle
│           ├── gradlew
│           └── gradlew.bat
├── start-gym-FE/
│   └── start-gym-FE/
│       ├── public/
│       ├── src/
│       ├── package.json
│       └── package-lock.json
├── final.sql
└── hd.txt
```

## Local Setup

### 1. Prerequisites

Install the following:

- JDK 8
- Node.js and npm (or Yarn)
- MySQL 8
- Git
- IntelliJ IDEA or another Java IDE

The instructions below assume the backend uses port `8080`, the frontend uses port `3000`, and MySQL uses port `3306`.

### 2. Clone the repository

```bash
git clone https://github.com/vuduy2909/DATN.git
cd DATN
```

### 3. Create and initialize the database

The checked-in backend configuration expects a MySQL database named `start_gym2`.

1. Start MySQL.
2. Create the database:

```sql
CREATE DATABASE start_gym2
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

3. Import the database dump from the repository root. For example, from a terminal with the MySQL client installed:

```bash
mysql -u root -p start_gym2 < final.sql
```

Enter your local MySQL password when prompted. If the SQL dump already contains database creation or `USE` statements, review it and follow the schema/database name it specifies before importing.

### 4. Configure the backend

Open this directory in IntelliJ IDEA:

```text
start-gym-BE/start-gym-BE/wellness-navigator-gym/
```

The main Spring Boot entry point is `WellnessNavigatorGymApplication.java`.

Before starting the backend, open:

```text
src/main/resources/application.properties
```

Update the settings for your own machine and accounts. At minimum, configure the database connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/start_gym2?allowPublicKeyRetrieval=true&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Also configure the integrations you intend to use:

| Setting | Purpose | Setup notes |
|---|---|---|
| `google.clientId`, `google.clientSecret` | Google sign-in | Create OAuth credentials in Google Cloud Console and configure authorized origins/redirect URIs for your environment. |
| `spring.mail.*` | Email delivery | Use Gmail SMTP with 2-Step Verification and an App Password; do not use your normal Gmail password. |
| `vnpay.*` | Payment flow | Use your own VNPay sandbox merchant credentials and a return URL reachable by your local application. |
| `jwt.secret`, `jwt.expiration` | Token signing and expiry | Replace the development secret with a strong, private secret. |
| `claude.api.key`, `claude.api.url`, `claude.api.version`, `claude.api.model` | Claude API | Supply your own API key and use a model available to your account. |
| Firebase service-account / storage settings | Firebase-backed features | Configure the required Firebase project credentials and storage settings expected by the backend. |

**Important:** The repository configuration has contained credential values. Treat any credential that was committed to a public repository as exposed: revoke or rotate it, then move secrets out of tracked files. Use environment variables or an untracked local configuration file, and never commit real passwords, API keys, JWT secrets, OAuth client secrets, Firebase service-account files, or payment secrets.

Keep the configured database name consistent with the database you created. The current JPA setting uses `spring.jpa.hibernate.ddl-auto=update`; for a local development setup, verify the schema before running against any existing database.

### 5. Start the backend

From the backend project directory, run on Windows:

```powershell
.\gradlew.bat bootRun
```

On macOS/Linux:

```bash
./gradlew bootRun
```

You can also import the Gradle project into IntelliJ IDEA and run `WellnessNavigatorGymApplication.java`.

The frontend's package configuration proxies API requests to `http://localhost:8080`, so make sure the backend is running on that port or update the proxy to match your setup.

### 6. Start the frontend

Open a second terminal:

```bash
cd start-gym-FE/start-gym-FE
npm install
npm start
```

The React development server typically opens at:

```text
http://localhost:3000
```

If using Yarn instead of npm, install dependencies with `yarn install` and start with `yarn start`.

### 7. Verify the API documentation

With the backend running, open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

These paths are configured in `application.properties`.

### 8. Optional: Run MySQL with Docker Compose

The repository includes `devops/docker-compose.yml`, which defines a MySQL 8 service. From the `devops` directory, provide the environment variables referenced by the Compose file:

- `DB_ROOT_PWD` — local MySQL root password
- `DB_NAME` — database name

Then start the service:

```bash
docker compose up -d
```

The Compose file publishes MySQL on port `3306` and persists its data in the `db_data` volume. Ensure the database name and credentials match the backend configuration.

## Deployment Notes

The `devops/` directory contains:

- `docker-compose.yml`: MySQL container and persistent storage.
- `caddyfile`: static frontend hosting and reverse proxy rules for the API.
- `ecosystem.config.js`: PM2 configuration for running the built Spring Boot JAR.

The deployment files contain environment-specific paths, hostnames, and addresses. Review and update them for your server before deploying. Never copy production credentials into the repository.

## License

No license information is specified in this repository. Contact the repository owner before reusing or redistributing the project.
