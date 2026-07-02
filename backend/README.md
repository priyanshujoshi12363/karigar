# Karigar — Backend

REST API for **Karigar**, an on-demand local-services app for tier-2/3 cities in India
(book electricians, plumbers, maids, cooks, mechanics and 40+ more services near you).

Built with **Node.js + Express + MongoDB (Mongoose)**.

## Features

- Phone-based auth for **users** and **workers** with JWT (OTP handled on the client via Firebase).
- **Field-level encryption** (AES-256-GCM) for sensitive PII — address, Aadhaar number, Aadhaar photo URL.
- **Cloudinary** upload for Aadhaar photos.
- **Geo search** — find workers within a radius using MongoDB `2dsphere` (`$geoNear`).
- **Reverse geocoding** via OpenStreetMap Nominatim (coordinates → address).
- **Instant-order dispatch** — notifies nearest matching worker first, falls through on reject/timeout, then an open job pool.
- Flat pricing: ₹150/hour + ₹2 platform fee.

## Tech stack

- Node.js (ES modules) · Express 5
- MongoDB · Mongoose
- JSON Web Tokens · bcrypt-free (phone/OTP auth)
- Cloudinary · Multer · OpenStreetMap Nominatim

## Getting started

```bash
# 1. install dependencies
cd backend
npm install

# 2. create your env file
cp .env.example .env
#   then fill in MONGODB_URI, Cloudinary keys, etc.

# 3. generate the secrets
node -e "console.log('JWT_SECRET=' + require('crypto').randomBytes(48).toString('hex'))"
node -e "console.log('ENCRYPTION_KEY=' + require('crypto').randomBytes(32).toString('hex'))"

# 4. run
npm run dev     # nodemon (auto-reload)
# or
npm start       # node server.js
```

Server runs on `http://localhost:4000` by default.

## Environment variables

| Variable | Description |
|----------|-------------|
| `PORT` | Server port (default 4000) |
| `MONGODB_URI` | MongoDB connection string |
| `JWT_SECRET` | Secret for signing JWTs |
| `JWT_EXPIRY` | Token lifetime (e.g. `30d`) |
| `ENCRYPTION_KEY` | 32-byte hex (64 chars) for AES-256-GCM field encryption |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Cloudinary credentials |
| `OFFER_TTL_SECONDS` | Seconds a worker has to accept an order before auto-skip |

> ⚠️ `ENCRYPTION_KEY` is the master key for all encrypted data. If lost, encrypted
> Aadhaar/address data is unrecoverable. Never commit `.env`.

## API overview

Base URL: `/api/v1`

### Auth (user)
| Method | Route | Auth | Body / Notes |
|--------|-------|------|--------------|
| POST | `/auth/register` | – | `phone, name, address?, coordinates?, expoToken?` |
| POST | `/auth/login` | – | `phone, expoToken?` |
| GET | `/auth/verify` | Bearer | validates JWT, returns user |
| GET | `/auth/me` | Bearer | full profile |
| PATCH | `/auth/address` | Bearer | `address?, coordinates?` (keeps history) |

### User dashboard
| Method | Route | Auth |
|--------|-------|------|
| GET | `/user/explore?lat=&lng=&radius=` | Bearer |
| GET | `/user/search?q=&category=&lat=&lng=&radius=` | Bearer |
| GET | `/user/workers/:id` | Bearer |
| GET | `/user/history` | Bearer |
| DELETE | `/user/history` | Bearer |

### Orders (user)
| Method | Route | Auth |
|--------|-------|------|
| GET | `/user/orders/quote?durationMinutes=` | Bearer |
| POST | `/user/orders` | Bearer |
| GET | `/user/orders` | Bearer |
| GET | `/user/orders/:id` | Bearer |
| POST | `/user/orders/:id/cancel` | Bearer |
| POST | `/user/orders/:id/pay` | Bearer |

### Worker
| Method | Route | Auth |
|--------|-------|------|
| GET | `/worker/categories?search=&type=` | – |
| POST | `/worker/register` | – (multipart: `aadharPhoto` file) |
| POST | `/worker/login` | – |
| GET | `/worker/me` | Bearer (worker) |
| PATCH | `/worker/location` | Bearer (worker) |
| GET | `/worker/nearby?lat=&lng=&radius=&category=` | Bearer (user) |
| GET | `/worker/orders/offers` | Bearer (worker) |
| POST | `/worker/orders/:id/respond` | Bearer (worker) — `action: accept\|reject` |
| GET | `/worker/orders/jobs` | Bearer (worker) |
| POST | `/worker/orders/:id/pick` | Bearer (worker) |
| POST | `/worker/orders/:id/start` | Bearer (worker) — `otp` |
| POST | `/worker/orders/:id/finish` | Bearer (worker) — `otp` |
| GET | `/worker/orders` | Bearer (worker) |

### Geo
| Method | Route | Auth |
|--------|-------|------|
| GET | `/geo/reverse?lat=&lng=` | – | coordinates → address |

## Project structure

```
backend/
├── server.js
└── src/
    ├── DB/            # mongoose connection
    ├── config/        # cloudinary
    ├── constants/     # worker categories (skilled/unskilled)
    ├── controllers/   # auth, user, worker, order, geo
    ├── middlewares/   # auth, worker auth, upload
    ├── models/        # user, worker, order
    ├── routes/        # route definitions
    ├── services/      # order dispatch engine
    └── utils/         # crypto, jwt, pricing, reverse geocode, cloudinary upload
```
