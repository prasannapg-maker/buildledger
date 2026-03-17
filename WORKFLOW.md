# BuildLedger IAM Service – Complete Workflow Guide

Identity and Access Management microservice for the **BuildLedger** Construction Contract & Vendor Management System.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [Setup & Installation](#setup--installation)
4. [Running in VS Code](#running-in-vs-code)
5. [Running in IntelliJ IDEA](#running-in-intellij-idea)
6. [API Overview](#api-overview)
7. [Workflow 1 – Admin Login](#workflow-1--admin-login)
8. [Workflow 2 – Admin Creates Internal User](#workflow-2--admin-creates-internal-user)
9. [Workflow 3 – First Login & Force Password Change](#workflow-3--first-login--force-password-change)
10. [Workflow 4 – Failed Login & Account Lock](#workflow-4--failed-login--account-lock)
11. [Workflow 5 – Password Reset Flow](#workflow-5--password-reset-flow)
12. [Workflow 6 – Vendor Registration & Document Upload](#workflow-6--vendor-registration--document-upload)
13. [Workflow 7 – Client Registration & Admin Approval](#workflow-7--client-registration--admin-approval)
14. [Workflow 8 – Token Refresh](#workflow-8--token-refresh)
15. [Workflow 9 – Logout](#workflow-9--logout)
16. [Workflow 10 – Internal Token Validation](#workflow-10--internal-token-validation)
17. [Workflow 11 – Admin Account Management](#workflow-11--admin-account-management)
18. [Workflow 12 – Audit Logs](#workflow-12--audit-logs)
19. [Full Workflow Diagram](#full-workflow-diagram)
20. [Database Tables](#database-tables)
21. [Roles & Permissions](#roles--permissions)
22. [Common Errors & Fixes](#common-errors--fixes)

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.2.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| JWT (JJWT) | 0.12.5 | Token generation & validation |
| Spring Data JPA | 3.x | Database ORM |
| MySQL | 8.0+ | Primary database |
| PostgreSQL | 15+ | Alternative database |
| H2 | - | In-memory DB for tests |
| Bucket4j | 8.10.1 | Rate limiting |
| Springdoc OpenAPI | 2.5.0 | Swagger UI |
| Maven | 3.9+ | Build tool |
| Lombok | 1.18.32 | Boilerplate reduction |

---

## Project Structure

```
iam-service
├── pom.xml
├── .env.example
├── README.md
└── src
    ├── main
    │   ├── java/com/buildledger/iam
    │   │   ├── IamServiceApplication.java
    │   │   ├── controller
    │   │   │   ├── AuthController.java
    │   │   │   ├── AdminUserController.java
    │   │   │   ├── VendorController.java
    │   │   │   ├── ClientController.java
    │   │   │   ├── InternalController.java
    │   │   │   └── HealthController.java
    │   │   ├── service
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   ├── VendorService.java
    │   │   │   ├── ClientService.java
    │   │   │   ├── AuditService.java
    │   │   │   ├── EmailService.java
    │   │   │   ├── FileStorageService.java
    │   │   │   ├── ExternalVerificationService.java
    │   │   │   ├── TokenBlacklistService.java
    │   │   │   ├── RateLimitingService.java
    │   │   │   └── AdminBootstrapService.java
    │   │   ├── repository
    │   │   │   ├── UserRepository.java
    │   │   │   ├── VendorRepository.java
    │   │   │   ├── VendorDocumentRepository.java
    │   │   │   ├── ClientRepository.java
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── RevokedTokenRepository.java
    │   │   │   ├── RefreshTokenRepository.java
    │   │   │   └── PasswordResetTokenRepository.java
    │   │   ├── entity
    │   │   │   ├── User.java
    │   │   │   ├── UserRole.java
    │   │   │   ├── AccountStatus.java
    │   │   │   ├── Vendor.java
    │   │   │   ├── VendorStatus.java
    │   │   │   ├── VendorDocument.java
    │   │   │   ├── DocumentStatus.java
    │   │   │   ├── Client.java
    │   │   │   ├── ClientStatus.java
    │   │   │   ├── AuditLog.java
    │   │   │   ├── RevokedToken.java
    │   │   │   ├── RefreshToken.java
    │   │   │   └── PasswordResetToken.java
    │   │   ├── dto
    │   │   │   ├── request
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── CreateUserRequest.java
    │   │   │   │   ├── UpdateUserRequest.java
    │   │   │   │   ├── PasswordChangeRequest.java
    │   │   │   │   ├── PasswordResetRequest.java
    │   │   │   │   ├── PasswordResetConfirmRequest.java
    │   │   │   │   ├── TokenRefreshRequest.java
    │   │   │   │   ├── VendorRegistrationRequest.java
    │   │   │   │   ├── ClientRegistrationRequest.java
    │   │   │   │   ├── PanVerificationRequest.java
    │   │   │   │   └── AuditLogRequest.java
    │   │   │   └── response
    │   │   │       ├── ApiResponse.java
    │   │   │       ├── LoginResponse.java
    │   │   │       ├── UserResponse.java
    │   │   │       ├── VendorResponse.java
    │   │   │       ├── VendorDocumentResponse.java
    │   │   │       ├── ClientResponse.java
    │   │   │       ├── TokenValidationResponse.java
    │   │   │       ├── AuditLogResponse.java
    │   │   │       └── VerificationResponse.java
    │   │   ├── security
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── UserDetailsServiceImpl.java
    │   │   │   └── UserPrincipal.java
    │   │   ├── config
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── SwaggerConfig.java
    │   │   │   ├── AppConfig.java
    │   │   │   └── JwtProperties.java
    │   │   ├── exception
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── AuthenticationFailedException.java
    │   │   │   ├── AccountLockedException.java
    │   │   │   ├── AccountDisabledException.java
    │   │   │   ├── InvalidTokenException.java
    │   │   │   ├── InvalidRequestException.java
    │   │   │   ├── PasswordMismatchException.java
    │   │   │   ├── RateLimitExceededException.java
    │   │   │   └── FileStorageException.java
    │   │   └── util
    │   │       ├── PasswordGenerator.java
    │   │       └── IpAddressUtil.java
    │   └── resources
    │       └── application.yml
    └── test
        ├── java/com/buildledger/iam
        │   ├── IamServiceApplicationTests.java
        │   └── service
        │       └── AuthServiceTest.java
        └── resources
            └── application.yml
```

---

## Setup & Installation

### Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 21 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads |
| VS Code or IntelliJ | Latest | https://code.visualstudio.com / https://www.jetbrains.com/idea |

### Step 1 – Install Java 21

Download Temurin 21 from https://adoptium.net and install it.

Verify:
```bash
java -version
# Expected: openjdk version "21.x.x"
```

### Step 2 – Install Maven

Download from https://maven.apache.org/download.cgi and extract to `C:\Maven`.

Add `C:\Maven\bin` to System PATH.

Verify:
```bash
mvn -version
# Expected: Apache Maven 3.9.x
```

### Step 3 – Create MySQL Database

Open MySQL Workbench or MySQL command line and run:
```sql
CREATE DATABASE buildledger_iam;
```

### Step 4 – Configure application.yml

Open `src/main/resources/application.yml` and update:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buildledger_iam?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root        # ← your MySQL username
    password: root        # ← your MySQL password
```

### Step 5 – Extract and Navigate

```bash
# Unzip the project
unzip buildledger-iam-service.zip

# Navigate to project folder
cd iam-service
```

---

## Running in VS Code

### Step 1 – Install VS Code Extensions

Open VS Code → Press `Ctrl + Shift + X` → Search and install:

| Extension | Publisher |
|-----------|-----------|
| Extension Pack for Java | Microsoft |
| Spring Boot Extension Pack | VMware |
| Maven for Java | Microsoft |

Restart VS Code after installing.

### Step 2 – Open the Project

```
File → Open Folder → Select the iam-service folder → Click Open
```

VS Code will show at the bottom:
```
Opening Java Project...
```
Wait 1–2 minutes for it to finish loading.

### Step 3 – Set Java 21 SDK

Press `Ctrl + Shift + P` → Type:
```
Java: Configure Java Runtime
```
Make sure Java 21 is selected. If not, click Download and pick Temurin 21.

### Step 4 – Update DB Config

Open `src/main/resources/application.yml` and update your MySQL credentials as shown in Setup Step 4.

### Step 5 – Run the Project

**Method A – Spring Boot Dashboard (Easiest)**

Look at the left sidebar → Click the Spring Boot Dashboard icon (🌿 leaf icon)

You will see:
```
iam-service
  └── IamServiceApplication
```
Click the ▶ Start button next to it.

**Method B – Run button in code**

Open:
```
src/main/java/com/buildledger/iam/IamServiceApplication.java
```
Click `Run` above the `main` method.

**Method C – Terminal**

Press `` Ctrl + ` `` to open terminal, then:
```bash
mvn spring-boot:run
```

### Step 6 – Confirm Running

Watch the terminal. You should see:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::               (v3.2.5)

Started IamServiceApplication on port 8081
```

### Step 7 – Open Swagger UI

```
http://localhost:8081/swagger-ui.html
```

---

## Running in IntelliJ IDEA

### Step 1 – Open the Project

```
File → Open → Select the iam-service folder → OK
```
IntelliJ will auto-detect Maven and download all dependencies automatically.

### Step 2 – Set Java 21 SDK

```
File → Project Structure → SDK → Select Java 21
```
If not available, click Add SDK → Download JDK → Version 21 (Temurin or Corretto).

### Step 3 – Update DB Config

Same as VS Code – update `application.yml` with your MySQL credentials.

### Step 4 – Run the Application

Open:
```
src/main/java/com/buildledger/iam/IamServiceApplication.java
```
Click the green ▶ Run button next to the `main` method.

### Step 5 – Open Swagger UI

```
http://localhost:8081/swagger-ui.html
```

---

## API Overview

| Group | Base Path | Auth Required |
|-------|-----------|---------------|
| Authentication | `/api/auth` | Public / Bearer |
| Admin Users | `/api/admin/users` | ADMIN only |
| Audit Logs | `/api/admin/audit-logs` | ADMIN only |
| Vendors | `/api/vendors` | Mixed |
| Clients | `/api/clients` | Mixed |
| Internal | `/api/internal` | Bearer |
| Health | `/api/health` | Public |

### Complete Endpoint List

```
── Authentication ──────────────────────────────────────
POST   /api/auth/login                        Login
POST   /api/auth/logout                       Logout (revoke tokens)
POST   /api/auth/token/refresh                Refresh access token
PUT    /api/auth/password                     Change password
POST   /api/auth/password/reset-request       Request password reset
POST   /api/auth/password/reset               Confirm password reset

── Admin User Management ───────────────────────────────
POST   /api/admin/users                       Create internal user
GET    /api/admin/users                       List all users
GET    /api/admin/users/{userId}              Get user by ID
PUT    /api/admin/users/{userId}              Update user
PATCH  /api/admin/users/{userId}/disable      Disable account
PATCH  /api/admin/users/{userId}/enable       Enable account
PATCH  /api/admin/users/{userId}/unlock       Unlock account
GET    /api/admin/audit-logs                  View audit logs

── Vendor ──────────────────────────────────────────────
POST   /api/vendors                           Vendor self-register (public)
GET    /api/vendors                           List all vendors (admin)
PATCH  /api/vendors/{vendorId}/approve        Approve vendor (admin)
PATCH  /api/vendors/{vendorId}/reject         Reject vendor (admin)
POST   /api/vendors/{vendorId}/documents      Upload document
GET    /api/vendors/{vendorId}/documents      Get vendor documents
GET    /api/vendors/{vendorId}/status         Get vendor status
GET    /api/vendors/verify-gst/{gstNumber}    Verify GST (admin)
POST   /api/vendors/verify-pan                Verify PAN (admin)
POST   /api/vendors/ocr-verify                OCR verification (admin)
PATCH  /api/vendors/documents/{docId}/verify  Verify document (admin)

── Client ──────────────────────────────────────────────
POST   /api/clients                           Client self-register (public)
GET    /api/clients                           List all clients (admin)
GET    /api/clients/{clientId}                Get client by ID (admin)
PATCH  /api/clients/{clientId}/approve        Approve client (admin)
PATCH  /api/clients/{clientId}/reject         Reject client (admin)

── Internal Microservice APIs ──────────────────────────
GET    /api/internal/auth/validate-token      Validate JWT token
GET    /api/internal/users/{userId}           Get user profile
GET    /api/internal/vendors/{vendorId}       Get vendor profile
GET    /api/internal/vendors/{vendorId}/status Get vendor status
POST   /api/internal/audit                    Submit audit log entry

── Health ──────────────────────────────────────────────
GET    /api/health                            Health check
GET    /management/health                     Spring Actuator health
```

---

## Workflow 1 – Admin Login

### Purpose
Admin authenticates and receives JWT tokens to access the system.

### Steps

**Step 1** – Open Swagger UI:
```
http://localhost:8081/swagger-ui.html
```

**Step 2** – Go to `Authentication → POST /api/auth/login` → Click **Try it out**

**Step 3** – Enter credentials:
```json
{
  "email": "admin@buildledger.com",
  "password": "Admin@2024!"
}
```

**Step 4** – Click **Execute**

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000-...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "name": "System Administrator",
      "email": "admin@buildledger.com",
      "role": "ADMIN"
    },
    "forcePasswordChange": false
  }
}
```

**Step 5** – Copy the `accessToken` value

**Step 6** – Click 🔒 **Authorize** button at the top of Swagger UI

**Step 7** – Paste the token in the Value field → Click **Authorize**

✅ Admin is now logged in and all admin endpoints are unlocked.

---

## Workflow 2 – Admin Creates Internal User

### Purpose
Admin creates internal users like Project Manager, Finance Officer, etc. System generates a temporary password and sends it to the user's email.

### Steps

**Step 1** – Make sure you are logged in as Admin (Workflow 1)

**Step 2** – Go to `Admin – User Management → POST /api/admin/users` → Click **Try it out**

**Step 3** – Enter user details:
```json
{
  "name": "Rahul Sharma",
  "email": "rahul@buildledger.com",
  "phone": "+919876543210",
  "role": "PROJECT_MANAGER"
}
```

Available roles: `ADMIN`, `PROJECT_MANAGER`, `FINANCE_OFFICER`, `COMPLIANCE_OFFICER`, `AUDIT_OFFICER`

**Step 4** – Click **Execute**

**Expected Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 2,
    "name": "Rahul Sharma",
    "email": "rahul@buildledger.com",
    "phone": "+919876543210",
    "role": "PROJECT_MANAGER",
    "status": "FIRST_LOGIN_REQUIRED",
    "forcePasswordChange": true,
    "createdAt": "2024-07-01T10:00:00"
  }
}
```

✅ User created. Temporary password sent to `rahul@buildledger.com`.

> **Note:** VENDOR and CLIENT roles cannot be created via this endpoint. They must self-register.

---

## Workflow 3 – First Login & Force Password Change

### Purpose
New user logs in with temporary password and is forced to set a new password before accessing the system.

### Steps

**Step 1** – Go to `Authentication → POST /api/auth/login` → **Try it out**

**Step 2** – Login with temporary credentials from email:
```json
{
  "email": "rahul@buildledger.com",
  "password": "TempPass@123"
}
```

**Expected Response:**
```json
{
  "data": {
    "accessToken": "eyJhbGci...",
    "forcePasswordChange": true,
    "user": {
      "role": "PROJECT_MANAGER"
    }
  }
}
```

**Step 3** – Notice `forcePasswordChange: true`. User MUST change password now.

**Step 4** – Copy the accessToken → Click **Authorize** in Swagger → Paste token

**Step 5** – Go to `Authentication → PUT /api/auth/password` → **Try it out**

**Step 6** – Enter password change request:
```json
{
  "currentPassword": "TempPass@123",
  "newPassword": "MyNew@Pass2024!",
  "confirmPassword": "MyNew@Pass2024!"
}
```

Password requirements:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@#$%^&+=!)

**Expected Response:**
```json
{
  "success": true,
  "message": "Password changed successfully. Please login again."
}
```

**Step 7** – Login again with new password:
```json
{
  "email": "rahul@buildledger.com",
  "password": "MyNew@Pass2024!"
}
```

**Expected Response:**
```json
{
  "data": {
    "forcePasswordChange": false,
    "user": {
      "status": "ACTIVE"
    }
  }
}
```

✅ User is now fully active and can access the system.

---

## Workflow 4 – Failed Login & Account Lock

### Purpose
System tracks failed login attempts. After 5 consecutive failures, account is automatically locked. Admin must unlock it.

### Steps

**Step 1** – Try logging in with wrong password (repeat 5 times):
```json
{
  "email": "rahul@buildledger.com",
  "password": "WrongPassword1"
}
```

After each failed attempt:
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

**Step 2** – After the 5th failed attempt, response changes to:
```json
{
  "success": false,
  "message": "Account locked due to multiple failed login attempts."
}
```

An email notification is also sent to the user's email.

**Step 3** – Even correct password now returns:
```json
{
  "success": false,
  "message": "Account locked due to multiple failed login attempts. Contact admin to unlock."
}
```

**Step 4** – Admin unlocks the account:

Login as admin → Go to `Admin → PATCH /api/admin/users/{userId}/unlock`

Enter `userId: 2` → Click **Execute**

**Expected Response:**
```json
{
  "success": true,
  "message": "User unlocked successfully",
  "data": {
    "id": 2,
    "status": "ACTIVE",
    "accountLocked": false,
    "failedLoginAttempts": 0
  }
}
```

✅ Account unlocked. User can now login normally.

> **Rate Limiting:** Additionally, more than 5 login attempts per minute from the same IP returns HTTP 429:
> ```json
> { "message": "Too many login attempts. Please wait 1 minute(s) before retrying." }
> ```

---

## Workflow 5 – Password Reset Flow

### Purpose
User forgets password and requests a reset via email. Token is valid for 15 minutes.

### Steps

**Step 1** – Go to `Authentication → POST /api/auth/password/reset-request` → **Try it out**

No auth required (public endpoint).

```json
{
  "email": "rahul@buildledger.com"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "If the email is registered, a password reset link has been sent."
}
```

> **Security Note:** Response is always the same whether email exists or not – prevents email enumeration attacks.

**Step 2** – User receives email with a reset token:
```
Token: abc123xyz456def789...
This token is valid for 15 minutes.
```

**Step 3** – Go to `Authentication → POST /api/auth/password/reset` → **Try it out**

```json
{
  "token": "abc123xyz456def789...",
  "newPassword": "Reset@Pass2024!",
  "confirmPassword": "Reset@Pass2024!"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Password reset successfully. Please login with your new password."
}
```

**Step 4** – If token is expired (after 15 minutes):
```json
{
  "success": false,
  "message": "Password reset token has expired or already been used"
}
```

✅ Password reset complete. User can now login with new password.

---

## Workflow 6 – Vendor Registration & Document Upload

### Purpose
External vendor company self-registers, uploads documents for verification, and admin approves them.

### Step A – Vendor Self-Registration (Public)

Go to `Vendor → POST /api/vendors` → **Try it out** (No login required)

```json
{
  "companyName": "BuildTech Solutions Pvt Ltd",
  "email": "contact@buildtech.com",
  "phone": "+919876543210",
  "category": "Civil Construction",
  "gstNumber": "29ABCDE1234F1Z5",
  "panNumber": "ABCDE1234F",
  "password": "Vendor@2024!"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Vendor registered successfully. Pending verification.",
  "data": {
    "id": 1,
    "companyName": "BuildTech Solutions Pvt Ltd",
    "email": "contact@buildtech.com",
    "category": "Civil Construction",
    "gstNumber": "29ABCDE1234F1Z5",
    "status": "PENDING_VERIFICATION",
    "gstVerified": false,
    "panVerified": false,
    "createdAt": "2024-07-01T11:00:00"
  }
}
```

### Step B – Vendor Uploads Document

Go to `Vendor → POST /api/vendors/{vendorId}/documents` → **Try it out**

- `vendorId`: `1`
- `file`: Upload a PDF or image file (max 10MB)
- `documentType`: `GST_CERTIFICATE`

Supported document types:
```
GST_CERTIFICATE
PAN_CARD
COMPANY_REGISTRATION
BANK_DETAILS
ADDRESS_PROOF
TRADE_LICENSE
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Document uploaded successfully. Pending verification.",
  "data": {
    "id": 1,
    "vendorId": 1,
    "documentType": "GST_CERTIFICATE",
    "originalFileName": "gst_certificate.pdf",
    "fileUrl": "/uploads/vendor-documents/vendor-1/gst_certificate_abc123.pdf",
    "status": "PENDING",
    "uploadedAt": "2024-07-01T11:05:00"
  }
}
```

### Step C – Admin Verifies GST via External API

Login as Admin → Go to `Vendor → GET /api/vendors/verify-gst/{gstNumber}`

Enter `gstNumber: 29ABCDE1234F1Z5` → Execute

**Expected Response (API available):**
```json
{
  "data": {
    "verified": true,
    "method": "API",
    "details": "GST is Active. Legal Name: BuildTech Solutions Pvt Ltd"
  }
}
```

**Expected Response (API unavailable – fallback to manual):**
```json
{
  "data": {
    "verified": false,
    "method": "MANUAL_PENDING",
    "details": "External API unavailable. Flagged for manual admin review.",
    "error": "GST API connection timeout"
  }
}
```

### Step D – Admin Verifies Document

Go to `Vendor → PATCH /api/vendors/documents/{documentId}/verify`

- `documentId`: `1`
- `status`: `VERIFIED`

**Expected Response:**
```json
{
  "data": {
    "id": 1,
    "status": "VERIFIED",
    "verifiedAt": "2024-07-01T11:30:00",
    "verifiedBy": "admin@buildledger.com"
  }
}
```

### Step E – Admin Approves Vendor

Go to `Vendor → PATCH /api/vendors/{vendorId}/approve`

Enter `vendorId: 1` → Execute

**Expected Response:**
```json
{
  "success": true,
  "message": "Vendor approved",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "approvedAt": "2024-07-01T12:00:00",
    "approvedBy": "admin@buildledger.com"
  }
}
```

An approval email is sent to the vendor automatically.

### Step F – Admin Rejects Vendor (Alternative)

Go to `Vendor → PATCH /api/vendors/{vendorId}/reject`

- `vendorId`: `1`
- `reason`: `GST number does not match company records`

**Expected Response:**
```json
{
  "data": {
    "status": "REJECTED",
    "rejectionReason": "GST number does not match company records"
  }
}
```

✅ Vendor workflow complete.

**Vendor Status Lifecycle:**
```
REGISTERED → PENDING_VERIFICATION → APPROVED
                                  → REJECTED
             APPROVED             → SUSPENDED
```

---

## Workflow 7 – Client Registration & Admin Approval

### Purpose
External client company registers for the BuildLedger platform. Admin reviews and approves or rejects them.

### Step A – Client Self-Registration (Public)

Go to `Client → POST /api/clients` → **Try it out** (No login required)

```json
{
  "companyName": "XYZ Infrastructure Corp",
  "email": "contact@xyz-infra.com",
  "phone": "+919123456789",
  "projectDescription": "Need construction services for a 5-floor commercial building in Bangalore. Budget approx 5 crores.",
  "password": "Client@2024!"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Client registration submitted. Pending approval.",
  "data": {
    "id": 1,
    "companyName": "XYZ Infrastructure Corp",
    "email": "contact@xyz-infra.com",
    "status": "PENDING_APPROVAL",
    "createdAt": "2024-07-01T13:00:00"
  }
}
```

### Step B – Admin Views Pending Clients

Login as Admin → Go to `Client → GET /api/clients`

Optional filter:
```
?status=PENDING_APPROVAL
```

**Expected Response:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "companyName": "XYZ Infrastructure Corp",
        "email": "contact@xyz-infra.com",
        "projectDescription": "Need construction services...",
        "status": "PENDING_APPROVAL"
      }
    ],
    "totalElements": 1
  }
}
```

### Step C – Admin Approves Client

Go to `Client → PATCH /api/clients/{clientId}/approve`

Enter `clientId: 1` → Execute

**Expected Response:**
```json
{
  "success": true,
  "message": "Client approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "approvedAt": "2024-07-01T13:30:00",
    "approvedBy": "admin@buildledger.com"
  }
}
```

### Step D – Admin Rejects Client (Alternative)

Go to `Client → PATCH /api/clients/{clientId}/reject`

- `clientId`: `1`
- `reason`: `Incomplete project details provided`

**Expected Response:**
```json
{
  "data": {
    "status": "REJECTED",
    "rejectionReason": "Incomplete project details provided"
  }
}
```

✅ Client workflow complete.

**Client Status Lifecycle:**
```
PENDING_APPROVAL → APPROVED
                → REJECTED
```

---

## Workflow 8 – Token Refresh

### Purpose
Access token expires after 15 minutes. Use the refresh token (valid 7 days) to get a new access token without re-logging in.

### Steps

**Step 1** – When you receive a 401 error:
```json
{
  "success": false,
  "message": "Unauthorized: JWT token expired"
}
```

**Step 2** – Go to `Authentication → POST /api/auth/token/refresh` → **Try it out**

**Step 3** – Enter the refresh token from your original login response:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000-..."
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...(new token)",
    "refreshToken": "661f9511-f3ac-52e5-b827-557766551111...(new refresh token)",
    "expiresIn": 900
  }
}
```

**Step 4** – Click **Authorize** in Swagger → Paste the new accessToken

✅ Session extended. Old refresh token is invalidated and a new one is issued.

> **If refresh token is expired or revoked:**
> ```json
> { "message": "Refresh token has expired or been revoked" }
> ```
> User must login again from scratch.

---

## Workflow 9 – Logout

### Purpose
User logs out. Access token is added to the blacklist. All refresh tokens are revoked. Any further use of the old token returns 401.

### Steps

**Step 1** – Make sure you are logged in (token set in Authorize)

**Step 2** – Go to `Authentication → POST /api/auth/logout` → **Try it out**

No request body needed. Just click **Execute**.

**Expected Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

**Step 3** – Try using the same token again on any endpoint:

**Expected Response:**
```json
{
  "success": false,
  "message": "Unauthorized: Token has been revoked"
}
```

✅ Logout complete. Token blacklisted in `revoked_tokens` table.

---

## Workflow 10 – Internal Token Validation

### Purpose
Other microservices (Contract Service, Finance Service, Compliance Service, etc.) call this endpoint to validate a user's JWT token and get their identity before processing requests.

### Steps

**Step 1** – Login as any user and get an access token

**Step 2** – Go to `Internal – Microservice APIs → GET /api/internal/auth/validate-token` → **Try it out**

The Authorization header is automatically sent from Swagger with the current token.

Click **Execute**

**Expected Response (valid token):**
```json
{
  "valid": true,
  "userId": 2,
  "role": "PROJECT_MANAGER",
  "email": "rahul@buildledger.com"
}
```

**Expected Response (invalid/expired token):**
```json
{
  "valid": false,
  "reason": "Token is invalid or expired"
}
```

**Expected Response (revoked/blacklisted token):**
```json
{
  "valid": false,
  "reason": "Token has been revoked"
}
```

**Expected Response (user account disabled):**
```json
{
  "valid": false,
  "reason": "User account is inactive or not found"
}
```

### How Other Services Use This

Every downstream microservice follows this pattern:

```
1. Receive HTTP request from user with Authorization: Bearer <token>
2. Call GET /api/internal/auth/validate-token
3. If valid: true → extract userId and role → process request
4. If valid: false → return 401 Unauthorized to user
```

✅ Central token validation for the entire BuildLedger ecosystem.

---

## Workflow 11 – Admin Account Management

### Purpose
Admin manages all user accounts – view, update, enable, disable, unlock.

### View All Users

Go to `Admin → GET /api/admin/users` → Execute

Optional query parameters:
```
?search=rahul          (search by name or email)
?page=0&size=20        (pagination)
?sortBy=createdAt&sortDir=DESC
```

**Expected Response:**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "name": "System Administrator",
        "email": "admin@buildledger.com",
        "role": "ADMIN",
        "status": "ACTIVE"
      },
      {
        "id": 2,
        "name": "Rahul Sharma",
        "email": "rahul@buildledger.com",
        "role": "PROJECT_MANAGER",
        "status": "ACTIVE"
      }
    ],
    "totalElements": 2,
    "totalPages": 1
  }
}
```

### Get Single User

Go to `Admin → GET /api/admin/users/{userId}`

Enter `userId: 2` → Execute

### Update User

Go to `Admin → PUT /api/admin/users/{userId}` → **Try it out**

Enter `userId: 2` and:
```json
{
  "name": "Rahul Kumar Sharma",
  "phone": "+919999999999",
  "role": "FINANCE_OFFICER"
}
```

### Disable User Account

Go to `Admin → PATCH /api/admin/users/{userId}/disable`

Enter `userId: 2` → Execute

```json
{
  "data": {
    "status": "DISABLED"
  }
}
```

User immediately gets 403 on their next request.

### Enable User Account

Go to `Admin → PATCH /api/admin/users/{userId}/enable`

Enter `userId: 2` → Execute

```json
{
  "data": {
    "status": "ACTIVE",
    "accountLocked": false
  }
}
```

### Unlock Locked Account

Go to `Admin → PATCH /api/admin/users/{userId}/unlock`

Enter `userId: 2` → Execute

```json
{
  "data": {
    "status": "ACTIVE",
    "accountLocked": false,
    "failedLoginAttempts": 0
  }
}
```

✅ All account state changes are recorded in audit logs automatically.

---

## Workflow 12 – Audit Logs

### Purpose
Every action in the system is automatically logged. Admin can view, filter, and export audit history.

### View All Audit Logs

Login as Admin → Go to `Admin → GET /api/admin/audit-logs` → Execute

**Expected Response:**
```json
{
  "data": {
    "content": [
      {
        "auditId": 1,
        "userId": 1,
        "action": "LOGIN",
        "resource": "users/1",
        "ipAddress": "127.0.0.1",
        "outcome": "SUCCESS",
        "timestamp": "2024-07-01T10:00:00"
      },
      {
        "auditId": 2,
        "userId": 1,
        "action": "USER_CREATED",
        "resource": "users/2",
        "ipAddress": "127.0.0.1",
        "outcome": "SUCCESS",
        "timestamp": "2024-07-01T10:05:00"
      },
      {
        "auditId": 3,
        "userId": 2,
        "action": "LOGIN_FAILED",
        "resource": "users/2",
        "ipAddress": "192.168.1.10",
        "details": "Invalid password – attempt 1",
        "outcome": "FAILURE",
        "timestamp": "2024-07-01T10:10:00"
      }
    ],
    "totalElements": 3
  }
}
```

### Filter Audit Logs

```
?userId=2                               (filter by user)
?action=LOGIN_FAILED                    (filter by action)
?from=2024-07-01T00:00:00              (from date)
?to=2024-07-31T23:59:59               (to date)
?page=0&size=50                        (pagination)
```

### All Tracked Actions

| Action | Trigger |
|--------|---------|
| `LOGIN` | Successful login |
| `LOGIN_FAILED` | Wrong password |
| `LOGOUT` | User logged out |
| `ACCOUNT_LOCKED` | Account locked after 5 failures |
| `USER_UNLOCKED` | Admin unlocked account |
| `PASSWORD_CHANGED` | User changed password |
| `PASSWORD_RESET_REQUESTED` | Reset email sent |
| `PASSWORD_RESET_COMPLETED` | Password reset done |
| `USER_CREATED` | Admin created a user |
| `USER_UPDATED` | Admin updated a user |
| `USER_DISABLED` | Admin disabled account |
| `USER_ENABLED` | Admin enabled account |
| `VENDOR_REGISTERED` | Vendor self-registered |
| `VENDOR_APPROVED` | Admin approved vendor |
| `VENDOR_REJECTED` | Admin rejected vendor |
| `DOCUMENT_UPLOADED` | Vendor uploaded document |
| `DOCUMENT_VERIFIED` | Admin verified document |
| `CLIENT_REGISTERED` | Client self-registered |
| `CLIENT_APPROVED` | Admin approved client |
| `CLIENT_REJECTED` | Admin rejected client |

✅ Complete audit trail for compliance and reporting.

---

## Full Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    BUILDLEDGER IAM SERVICE                       │
└─────────────────────────────────────────────────────────────────┘

VENDOR FLOW
───────────
Vendor → POST /api/vendors (self-register)
       → Status: PENDING_VERIFICATION
       → POST /api/vendors/{id}/documents (upload GST, PAN, etc.)
       → Admin: GET /api/vendors/verify-gst/{gst}
       → Admin: POST /api/vendors/verify-pan
       → Admin: PATCH /api/vendors/{id}/approve
       → Status: APPROVED ✅

CLIENT FLOW
───────────
Client → POST /api/clients (self-register)
       → Status: PENDING_APPROVAL
       → Admin: GET /api/clients
       → Admin: PATCH /api/clients/{id}/approve
       → Status: APPROVED ✅

INTERNAL USER FLOW
──────────────────
Admin → POST /api/admin/users (create user with role)
      → Email sent with temp password
      → User: POST /api/auth/login (forcePasswordChange: true)
      → User: PUT /api/auth/password (set new password)
      → Status: ACTIVE ✅

AUTHENTICATION FLOW
───────────────────
User  → POST /api/auth/login
      → accessToken (15 min) + refreshToken (7 days)
      → [use accessToken for all requests]
      → Token expires → POST /api/auth/token/refresh
      → POST /api/auth/logout → Token blacklisted

SECURITY FLOW
─────────────
Login attempt → Rate limit check (5/min per IP)
              → Wrong password → failedAttempts++
              → 5 failures → Account LOCKED
              → Admin: PATCH /api/admin/users/{id}/unlock

INTERNAL SERVICE FLOW
─────────────────────
Contract Service  ─┐
Finance Service   ─┤─→ GET /api/internal/auth/validate-token
Compliance Service─┤   → { valid: true, userId, role }
Reporting Service ─┘
```

---

## Database Tables

| Table | Records | Purpose |
|-------|---------|---------|
| `users` | All system users | Internal staff, admin |
| `vendors` | Vendor companies | Registered vendor profiles |
| `vendor_documents` | Uploaded files | Document metadata and status |
| `clients` | Client companies | Registered client profiles |
| `refresh_tokens` | Active sessions | JWT refresh token store |
| `revoked_tokens` | Blacklisted JWTs | Logout token blacklist |
| `password_reset_tokens` | Reset flow | 15-minute reset tokens |
| `audit_logs` | All actions | Complete system audit trail |

---

## Roles & Permissions

| Role | Can Access |
|------|-----------|
| `ADMIN` | All endpoints |
| `PROJECT_MANAGER` | Contracts, vendors, reports |
| `FINANCE_OFFICER` | Finance, invoices, payments |
| `COMPLIANCE_OFFICER` | Compliance reports, documents |
| `AUDIT_OFFICER` | Read-only audit access |
| `VENDOR` | Own profile, documents, contracts |
| `CLIENT` | Own profile, project status |

---

## Common Errors & Fixes

| HTTP Code | Error Message | Cause | Fix |
|-----------|---------------|-------|-----|
| `401` | Invalid email or password | Wrong credentials | Check email/password |
| `401` | JWT token expired | Token older than 15 min | Use refresh token |
| `401` | Token has been revoked | Logged out | Login again |
| `403` | Account locked | 5 failed attempts | Admin unlock |
| `403` | Account has been disabled | Admin disabled | Contact admin |
| `403` | Access Denied: insufficient privileges | Wrong role | Use correct role account |
| `404` | User not found with id: X | Wrong userId | Check userId |
| `409` | User with email already exists | Duplicate email | Use different email |
| `400` | Validation failed | Missing/invalid fields | Check request body |
| `400` | Passwords do not match | confirmPassword wrong | Re-enter passwords |
| `429` | Too many login attempts | Rate limit hit | Wait 1 minute |
| `413` | File size exceeds maximum limit | File > 10MB | Use smaller file |
| `500` | Internal server error | Server crash | Check logs |

### Common Setup Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `Port 8081 already in use` | Another app on 8081 | Change `server.port` in `application.yml` to `8082` |
| `Access denied for user 'root'` | Wrong DB password | Update password in `application.yml` |
| `Unknown database buildledger_iam` | DB not created | Run `CREATE DATABASE buildledger_iam;` in MySQL |
| `Java 21 not found` | Wrong Java version | Install Temurin 21 from adoptium.net |
| `mvn not found` | Maven not in PATH | Add Maven bin to System PATH |

---

## Quick Test Checklist

```
☐ 1.  Service starts on port 8081
☐ 2.  Swagger UI loads at /swagger-ui.html
☐ 3.  Admin login returns accessToken
☐ 4.  Authorize button accepts token
☐ 5.  GET /api/admin/users returns user list
☐ 6.  POST /api/admin/users creates user
☐ 7.  New user login shows forcePasswordChange: true
☐ 8.  PUT /api/auth/password changes password
☐ 9.  POST /api/vendors registers vendor
☐ 10. POST /api/clients registers client
☐ 11. PATCH /api/vendors/{id}/approve approves vendor
☐ 12. PATCH /api/clients/{id}/approve approves client
☐ 13. 5 wrong passwords → account locked
☐ 14. PATCH /api/admin/users/{id}/unlock → unlocks
☐ 15. POST /api/auth/logout → 401 on reuse
☐ 16. POST /api/auth/token/refresh → new token
☐ 17. GET /api/internal/auth/validate-token → valid: true
☐ 18. GET /api/admin/audit-logs → shows all actions
☐ 19. GET /api/health → status UP
```

---

*BuildLedger IAM Service v1.0.0 – Engineering Team*
