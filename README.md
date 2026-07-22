# E-Governance Web

Full-stack municipal e-governance web application with an Angular frontend, Spring Boot backend, and MySQL database dump.

## Project Structure

```text
E-Governance_Web/
├── municipality-dashboard/   # Angular frontend
├── governance/               # Spring Boot backend
├── govt.sql                  # MySQL database dump
└── README.md
```

## Main Modules

- Citizen services: birth/death certificate, citizen certificate, family certificate, passport, application status
- Trade license: new license, renewal, inspections, PDF generation
- Holding tax: registration, assessment, due list, payment, reports
- Infrastructure: road, drainage, street light, construction permission, admin status view
- Health and sanitation: EPI registration/admin, health notices, sanitation monitoring, health center info
- E-Tender: notices, bids, awards, vendor blacklist
- E-Voting: voter registration/approval, candidate management, voting, result, audit
- Payment: payment gateway, payment history, admin monitoring, receipt PDF
- Water supply: connection application, billing, bill status/payment, usage
- Waste management: pickup request, schedule, collection report, smart bin
- Social cards: VGD, family, farmer, LPG card application/admin/status
- Maps, notices, complaints, communication, reports, wards, roles and permissions

## Requirements

- Node.js and npm
- Angular CLI 13
- Java 17
- Maven
- MySQL 8+

## Database Setup

1. Create a MySQL database named `govt`.
2. Import the database dump:

```bash
mysql -u root -p govt < govt.sql
```

The backend is configured to connect to:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/govt
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:root}
```

For local development, either keep MySQL root password as `root` or set `DB_PASSWORD`.

## Backend Setup

Go to the backend folder:

```bash
cd governance
```

Run the application:

```bash
mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

API base URL:

```text
http://localhost:8080/api
```

Useful environment variables:

```text
DB_PASSWORD
MAIL_USERNAME
MAIL_PASSWORD
MAIL_FROM
JWT_ACCESS_SECRET
JWT_REFRESH_SECRET
APP_UPLOAD_DIR
APP_CORS_ALLOWED_ORIGIN_PATTERNS
OTP_DEV_MODE
```

## Frontend Setup

Go to the frontend folder:

```bash
cd municipality-dashboard
```

Install dependencies:

```bash
npm install
```

Run the Angular app:

```bash
npm start
```

The frontend runs on:

```text
http://localhost:4200
```

Frontend environment:

```ts
apiUrl: 'http://localhost:8080/api'
serverUrl: 'http://localhost:8080'
```

## Build Commands

Frontend build:

```bash
cd municipality-dashboard
npm run build
```

Backend tests:

```bash
cd governance
mvnw.cmd test
```

## GitHub Repository

Repository:

```text
https://github.com/sefatm/E-Governance_Web.git
```

## Notes

- Runtime uploads, build outputs, Angular cache, Maven target files, and local tool metadata are ignored by Git.
- Configure real production secrets through environment variables before deployment.
- The project is intended for local municipal e-governance workflow testing and development.
