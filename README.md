# Multi-Tenant AI Analytics Platform

A production-grade SaaS analytics platform with multi-tenant architecture, AI-powered insights, and role-based access control.

## Tech Stack

### **Backend**

- Java 17 + Spring Boot 4.0.1
- Spring Security 6 (JWT via HttpOnly cookies)
- Hibernate 6 (multi-tenancy: SCHEMA mode)
- PostgreSQL + Flyway 12 migrations
- Lombok, SpringDoc OpenAPI 3 (springdoc-openapi 3.0.1)

### **Frontend**

- Next.js 14 (App Router)
- TypeScript
- TailwindCSS + ShadCN UI (Radix primitives)
- Zustand (auth state)
- Recharts (data visualization)
- React Hook Form + Zod (validation)

---

## Architecture Overview

### Multi-Tenancy

Each organization gets its own PostgreSQL schema (`tenant_{slug}`). The public schema holds shared entities (users, organizations, memberships, invitations). Tenant routing happens per-request via:

```text
JWT cookie → JwtAuthFilter → TenantResolutionFilter → TenantContextHolder → TenantIdentifierResolver → Hibernate SET search_path
```

### AI Insights

`AiInsightEngine` is the core interface. `MockAiInsightEngine` is active by default (`@Primary`). Claude and OpenAI engines are available as `@Profile` stubs — swap by activating the appropriate Spring profile and providing API keys.

---

## Project Structure

```text
.
├── backend/                        # Spring Boot application
│   └── src/main/java/com/platform/analytics/
│       ├── ai/                     # AI engine interface + implementations
│       ├── config/                 # Security, CORS, OpenAPI, JPA config
│       ├── controller/             # REST controllers
│       ├── dto/                    # Request/response DTOs
│       ├── exception/              # Custom exceptions + global handler
│       ├── model/                  # JPA entities
│       ├── repository/             # Spring Data JPA repositories
│       ├── security/               # JWT filter, tenant filter, UserPrincipal
│       ├── seeder/                 # Dev seed data (DataSeeder)
│       ├── service/                # Business logic
│       └── tenant/                 # Multi-tenancy infrastructure
│   └── src/main/resources/
│       ├── application.yml         # Base configuration
│       ├── application-dev.yml     # Dev overrides (seed enabled, SQL logging)
│       └── db/migration/           # Flyway SQL migrations
│
└── frontend/                       # Next.js application
    ├── app/
    │   ├── (auth)/                 # Login, Register pages
    │   └── (dashboard)/            # Protected dashboard pages
    │       ├── page.tsx            # Overview / KPI dashboard
    │       ├── datasets/           # Dataset list + detail
    │       ├── insights/           # AI insight list + detail
    │       ├── users/              # User management
    │       └── settings/           # Organization settings
    ├── components/
    │   ├── layout/                 # Header, Sidebar, PageContainer
    │   ├── ui/                     # ShadCN base components
    │   ├── dashboard/              # KpiCard, RevenueChart
    │   ├── datasets/               # UploadModal, DatasetTable
    │   ├── insights/               # InsightCard, GenerateInsightModal
    │   └── users/                  # UserTable, InviteUserModal, RoleSelector
    ├── lib/api.ts                  # Typed API client (fetch + credentials)
    ├── store/useAuthStore.ts        # Zustand auth store
    └── types/index.ts              # Shared TypeScript types
```

---

## Prerequisites

- Java 17+
- Maven 3.6.3+ (tested with 3.9.12)
- Node.js 18+ and npm
- PostgreSQL 14+

---

## Getting Started

### 1. Database Setup

Create the database and user:

```sql
CREATE USER analytics WITH PASSWORD 'analytics';
CREATE DATABASE analytics_platform OWNER analytics;
GRANT ALL PRIVILEGES ON DATABASE analytics_platform TO analytics;
```

### 2. Backend

```bash
cd backend

# Run with dev profile (enables seed data)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The API starts on `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

**Environment variables** (optional overrides):

| Variable          | Default                        | Description                  |
|-------------------|--------------------------------|------------------------------|
| `DB_USER`         | `analytics`                    | PostgreSQL username          |
| `DB_PASSWORD`     | `analytics`                    | PostgreSQL password          |
| `JWT_SECRET`      | (hardcoded dev secret)         | JWT signing key              |
| `CORS_ORIGINS`    | `http://localhost:3000`        | Allowed frontend origin      |
| `COOKIE_SECURE`   | `false`                        | Set `true` in production     |
| `FILE_UPLOAD_DIR` | `./uploads`                    | CSV upload directory         |

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:3000`.

---

## Seed Data (Dev Profile)

When running with `--spring.profiles.active=dev`, the `DataSeeder` creates two organizations with sample users and datasets:

| Email                | Password     | Role   | Organization |
|----------------------|--------------|--------|--------------|
| `alice@acme.com`     | Password123! | OWNER  | Acme Corp    |
| `carol@acme.com`     | Password123! | MEMBER | Acme Corp    |
| `dave@acme.com`      | Password123! | VIEWER | Acme Corp    |
| `david@techstart.io` | Password123! | OWNER  | TechStart    |

---

## API Overview

All endpoints are prefixed with `/api`. Authentication uses HttpOnly JWT cookies set on login.

| Method | Endpoint                         | Description                     | Min Role |
|--------|----------------------------------|---------------------------------|----------|
| POST   | `/api/auth/register`             | Register + create organization  | Public   |
| POST   | `/api/auth/login`                | Login (sets JWT cookie)         | Public   |
| POST   | `/api/auth/logout`               | Clear JWT cookie                | Any      |
| GET    | `/api/auth/me`                   | Current user + organization     | Any      |
| GET    | `/api/datasets`                  | List datasets (paginated)       | VIEWER   |
| POST   | `/api/datasets`                  | Upload CSV dataset              | MEMBER   |
| GET    | `/api/datasets/{id}`             | Dataset detail with columns     | VIEWER   |
| DELETE | `/api/datasets/{id}`             | Delete dataset                  | ADMIN    |
| GET    | `/api/insights`                  | List AI insights (paginated)    | VIEWER   |
| POST   | `/api/insights/generate`         | Generate insight for dataset    | MEMBER   |
| GET    | `/api/insights/{id}`             | Insight detail                  | VIEWER   |
| GET    | `/api/users`                     | List org members (paginated)    | ADMIN    |
| POST   | `/api/users/invite`              | Invite user by email            | ADMIN    |
| PATCH  | `/api/users/{id}/role`           | Update member role              | ADMIN    |
| DELETE | `/api/users/{id}`                | Remove member from org          | OWNER    |
| GET    | `/api/organization`              | Get organization details        | Any      |
| PUT    | `/api/organization`              | Update organization name/slug   | OWNER    |
| GET    | `/api/audit`                     | Audit log (paginated)           | ADMIN    |

---

## Roles

| Role   | Capabilities                                                      |
|--------|-------------------------------------------------------------------|
| OWNER  | Full access — all admin actions + remove members + delete org     |
| ADMIN  | Invite/remove members, change roles, manage datasets              |
| MEMBER | Upload datasets, generate insights                                |
| VIEWER | Read-only access to datasets and insights                         |

---

## AI Engines

Switch the active AI engine via Spring profiles:

```bash
# Use Claude (requires ANTHROPIC_API_KEY env var)
mvn spring-boot:run -Dspring-boot.run.profiles=dev,claude

# Use OpenAI (requires OPENAI_API_KEY env var)
mvn spring-boot:run -Dspring-boot.run.profiles=dev,openai

# Default: mock engine (no API key needed)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Production Checklist

- [ ] Set a strong `JWT_SECRET` (min 256-bit random key)
- [ ] Set `COOKIE_SECURE=true` (HTTPS only)
- [ ] Set `CORS_ORIGINS` to your production frontend domain
- [ ] Use strong database credentials (not defaults)
- [ ] Disable `flyway.clean-disabled=false` (it is dev-only)
- [ ] Set `app.seed.enabled=false` (or omit dev profile)
- [ ] Configure `FILE_UPLOAD_DIR` to a persistent volume
- [ ] Set `logging.level` to `WARN` or `ERROR`
