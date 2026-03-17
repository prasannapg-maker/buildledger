# BuildLedger IAM Service

Identity and Access Management microservice for the **BuildLedger** Construction Contract & Vendor Management System.

---

## Quick Start

### 1. Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.0+ (or PostgreSQL 15+) |

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your DB credentials, JWT secret, SMTP settings, etc.
```

### 3. Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/iam-service-1.0.0.jar

# Or with Maven
mvn spring-boot:run
```

### 4. Swagger UI

Open: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## API Overview

| Group | Base Path | Auth |
|-------|-----------|------|
| Authentication | `/api/auth` | Public / Bearer |
| Admin Users | `/api/admin/users` | ADMIN only |
| Audit Logs | `/api/admin/audit-logs` | ADMIN only |
| Vendors | `/api/vendors` | Mixed |
| Clients | `/api/clients` | Mixed |
| Internal | `/api/internal` | Bearer |
| Health | `/api/health` | Public |

### Key Endpoints

```
POST   /api/auth/login                       Login
POST   /api/auth/logout                      Logout (revoke tokens)
POST   /api/auth/token/refresh               Refresh access token
PUT    /api/auth/password                    Change password
POST   /api/auth/password/reset-request      Request password reset
POST   /api/auth/password/reset              Confirm password reset

POST   /api/admin/users                      Create internal user
GET    /api/admin/users                      List users
GET    /api/admin/users/{id}                 Get user
PUT    /api/admin/users/{id}                 Update user
PATCH  /api/admin/users/{id}/disable         Disable account
PATCH  /api/admin/users/{id}/enable          Enable account
PATCH  /api/admin/users/{id}/unlock          Unlock account
GET    /api/admin/audit-logs                 View audit logs

POST   /api/vendors                          Vendor self-register
POST   /api/vendors/{id}/documents           Upload document
GET    /api/vendors/{id}/status              Get status
GET    /api/vendors/verify-gst/{gst}         Verify GST
POST   /api/vendors/verify-pan               Verify PAN
POST   /api/vendors/ocr-verify               OCR document

POST   /api/clients                          Client self-register
PATCH  /api/clients/{id}/approve             Approve client
PATCH  /api/clients/{id}/reject              Reject client

GET    /api/internal/auth/validate-token     Token validation
GET    /api/internal/users/{id}              Get user (internal)
GET    /api/internal/vendors/{id}            Get vendor (internal)
POST   /api/internal/audit                   Log audit event
```

---

## Security Model

- **JWT Access Token**: 15 minutes validity
- **JWT Refresh Token**: 7 days validity
- **Token Blacklist**: Revoked tokens stored in `revoked_tokens` table
- **Login Rate Limit**: 5 attempts per minute per IP (Bucket4j)
- **Account Lock**: After 5 consecutive failed login attempts
- **Password Policy**: Min 8 chars, must include upper, lower, digit, special char
- **First Login**: Forced password change on temporary password

---

## Roles

| Role | Description |
|------|-------------|
| `ADMIN` | Full system access |
| `PROJECT_MANAGER` | Contract & project oversight |
| `FINANCE_OFFICER` | Financial transactions |
| `COMPLIANCE_OFFICER` | Regulatory compliance |
| `AUDIT_OFFICER` | Read-only audit access |
| `VENDOR` | External vendor company |
| `CLIENT` | External client company |

---

## Downstream Services Integration

Other services validate tokens by calling:

```
GET /api/internal/auth/validate-token
Authorization: Bearer <user_token>

Response:
{
  "valid": true,
  "userId": 101,
  "role": "PROJECT_MANAGER",
  "email": "user@buildledger.com"
}
```

---

## Database Tables

| Table | Purpose |
|-------|---------|
| `users` | Internal users and vendor/client user accounts |
| `vendors` | Vendor company profiles |
| `vendor_documents` | Uploaded vendor documents |
| `clients` | Client company profiles |
| `refresh_tokens` | Active refresh tokens |
| `revoked_tokens` | JWT blacklist (logout) |
| `password_reset_tokens` | Password reset flow |
| `audit_logs` | All system actions |

---

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory database (no MySQL required for tests).
