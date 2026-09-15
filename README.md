# EBloodDonation — Production-Ready Online Blood Donation Platform

A comprehensive, production-ready emergency blood donation application ecosystem consisting of a native Android application (Kotlin + Jetpack Compose + Room Cache), a scalable Node.js/Express backend, a PostgreSQL relational database with Sequelize ORM, Firebase Phone Authentication & Cloud Messaging (FCM), and a secure administrative dashboard.

---

## 🏗️ Architecture Overview

```
                          ┌──────────────────────────┐
                          │   Firebase Services      │
                          │ - Phone Auth (SMS OTP)   │
                          │ - Cloud Messaging (FCM)  │
                          └─────────────▲────────────┘
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 │                      │                      │
┌────────────────┴─────────────┐        │        ┌─────────────┴────────────┐
│      Android Client App      │        │        │   Admin Control Panel    │
│  - Kotlin & Jetpack Compose  │        │        │   - Web HTML5 / JS (SPA) │
│  - Room Local Cache          │        │        │   - JWT Auth & Controls  │
│  - Emergency Siren & Ringtone│        │        │   - Live Settings Sync   │
└───────────────▲──────────────┘        │        └─────────────▲────────────┘
                │                       │                      │
                │     HTTPS REST API    │   Admin Endpoints    │
                └───────────────┬───────┴──────────────┬───────┘
                                │                      │
                        ┌───────▼──────────────────────▼───────┐
                        │        Node.js Backend Service       │
                        │       - Express.js & Helmet          │
                        │       - Rate-Limiting & JWT          │
                        │       - Firebase Admin SDK           │
                        └──────────────────┬───────────────────┘
                                           │
                                  Sequelize Connection
                                           │
                                ┌──────────▼──────────┐
                                │ PostgreSQL Database │
                                │  - Donors           │
                                │  - BloodRequests    │
                                │  - Donations        │
                                │  - AppSettings      │
                                │  - AdminUsers       │
                                └─────────────────────┘
```

---

## 🚀 Key Features

1. **Urgent Blood Request Matching**:
   - Location-based donor discovery with blood group compatibility filters.
   - Emergency 3-minute continuous siren and heads-up notification dispatched to compatible donors via Firebase Cloud Messaging.
   - 30-second call ringtone alert when a donor accepts a request.
   - Secure reveal of phone contact details only upon mutual confirmation.

2. **Security & Production Hardening**:
   - **Zero Hardcoded Passwords**: Android client authenticates against backend REST endpoints; admin credentials are encrypted with `bcrypt` in PostgreSQL and seeded via environment variables.
   - **Real Firebase Phone Auth**: No universal or backdoor OTP codes (`114300` removed).
   - **Rate-Limiting & Protection**: Helmet security headers, CORS origin validation, and express rate-limiting to prevent brute force.

3. **Dynamic App Settings & Wallet Support**:
   - Payment method default set to **Wallet** with configurable account details managed dynamically via the Admin Panel.
   - Real-time synchronization of logos, emergency notices, support hotlines, and maintenance mode across both mobile and web.

---

## 🛠️ Tech Stack

- **Android App**: Kotlin 2.1, Jetpack Compose, Material 3, Room, OkHttp, Coroutines, StateFlow, Navigation Compose.
- **Backend**: Node.js, Express, Sequelize ORM, PostgreSQL (`pg`, `pg-hstore`), Firebase Admin SDK, JWT (`jsonwebtoken`), Bcrypt.
- **Infrastructure**: Render deployment configuration (`render.yaml`), Docker containerization support.

---

## 📦 Deployment & Environment Setup

### 1. Backend Environment Variables (`backend/.env`)

Copy `backend/.env.example` to `backend/.env`:

```bash
PORT=5000
NODE_ENV=production
DATABASE_URL=postgresql://user:password@host:5432/eblooddonation?sslmode=require
JWT_SECRET=your_production_secret_key_change_me
DEFAULT_ADMIN_USERNAME=ashikbillah4300@gmail.com
DEFAULT_ADMIN_EMAIL=ashikbillah4300@gmail.com
DEFAULT_ADMIN_PASSWORD=your_secure_admin_password_here
FIREBASE_PROJECT_ID=eblooddonation-6af67
FIREBASE_CLIENT_EMAIL=firebase-adminsdk@eblooddonation-6af67.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgk...\n-----END PRIVATE KEY-----\n"
ALLOWED_ORIGINS=https://eblooddonation.onrender.com,http://localhost:3000
```

### 2. Render Deployment (`render.yaml`)

Deploy directly to [Render](https://render.com) using the included Blueprint:

1. Connect your GitHub repository to Render.
2. Render detects `render.yaml` and provisions:
   - **PostgreSQL Database** (`eblood-postgres`)
   - **Web Service** (`eblood-backend`) running Node.js in `./backend`
3. In the Render Dashboard, configure:
   - `JWT_SECRET`
   - `DEFAULT_ADMIN_PASSWORD`
   - `FIREBASE_CLIENT_EMAIL` and `FIREBASE_PRIVATE_KEY`
4. The service will build (`npm install`), run Sequelize migrations/sync, and seed the default admin securely.

### 3. Android Client Configuration

In the Android application:
- Enter your backend URL (e.g., `https://eblooddonation.onrender.com`) via the in-app Admin sync screen or set in `SessionManager`.
- Place your `google-services.json` in the `/app` module directory for Firebase Phone Auth and FCM push notifications.

---

## 📡 REST API Endpoints

### Public & User Endpoints
- `GET  /api/health` — Service health status
- `POST /api/auth/phone-login` — Verify phone authentication session
- `POST /api/auth/register-user` — Register or update donor profile
- `POST /api/auth/fcm-token` — Register device FCM push token
- `GET  /api/settings` — Get public application settings (Wallet info, emergency notices, logos)
- `GET  /api/donors` — Query compatible donors by blood group and location
- `POST /api/requests` — Create urgent blood request (fires FCM notifications to matching donors)
- `GET  /api/requests/:id` — Get request details
- `PATCH /api/requests/:id/status` — Accept or reject request
- `POST /api/donations` — Record completed blood donation

### Admin Endpoints (Protected by Bearer JWT)
- `POST /api/admin/login` — Authenticate admin credentials and retrieve JWT
- `GET  /api/admin/dashboard` — Platform overview statistics
- `GET  /api/admin/users` — List all registered donors with filters
- `GET  /api/admin/requests` — List all blood requests
- `GET  /api/admin/donations` — List all donation history records
- `GET  /api/admin/settings` — Read all system configurations
- `PATCH /api/admin/settings` — Bulk update settings (Wallet details, branding, notices)
- `POST /api/admin/upload-logo` — Upload new application logo

---

## 🔒 Security Best Practices Implemented

- All passwords hashed using `bcrypt` (salt rounds: 10).
- No hardcoded passwords, fixed OTP codes, or bypass flags exist in the source code.
- Admin login incorporates rate-limiting and account lockout mechanisms.
- Database access uses parameterized queries via Sequelize ORM to prevent SQL injection.
- Security headers enforced with `helmet` and strict CORS policy.
