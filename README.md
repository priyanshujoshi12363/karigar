# Karigar 🛠️

**On-demand local services for tier-2 and tier-3 cities in India.**

Karigar connects people with nearby skilled and unskilled workers — electricians, plumbers,
maids, cooks, mechanics, carpenters, farm labour and 40+ more — with flat, transparent pricing
(₹150/hour + ₹2 platform fee), live location matching, and instant order dispatch.

This is a monorepo containing the **backend API** and the **Android app**.

```
karigar/
├── backend/     Node.js + Express + MongoDB REST API
└── karigar/     Android app (Kotlin + Jetpack Compose)
```

---

## ✨ Features

- 📱 **Phone + OTP auth** for users and workers (JWT sessions)
- 📍 **Find workers within 5 km** using MongoDB geo queries (`2dsphere`)
- ⚡ **Instant order dispatch** — nearest matching worker is notified first, falls through on
  reject/timeout, then lands in an open job pool
- 🔐 **Field-level encryption** (AES-256-GCM) for sensitive data (address, Aadhaar number & photo)
- 🗺️ **Map-based location confirmation** (OpenStreetMap, no API key)
- 💳 **Start/End OTP + payment flow** — ₹150 to the worker, ₹2 platform fee
- 🌗 **Light & dark theme** that follows the device (white/blue in light, black/gray/white with blue accents in dark)

---

## 🧱 Tech stack

| Layer | Technology |
|-------|------------|
| Backend | Node.js, Express 5, MongoDB (Mongoose), JWT, Cloudinary, Multer, OpenStreetMap Nominatim |
| Android | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit + OkHttp, osmdroid |

---

## 🚀 Getting started

### Backend
```bash
cd backend
npm install
cp .env.example .env      # then fill in your values
npm run dev               # runs on http://localhost:4000
```
See [`backend/README.md`](backend/README.md) for env variables and the full API reference.

### Android app
```bash
cd karigar
# open in Android Studio, or from the terminal:
./gradlew assembleDebug   # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug    # installs on a connected device/emulator
```

> The app talks to the backend at `http://10.0.2.2:4000` (emulator → host).
> For a physical phone, set `BASE_URL` in `karigar/app/src/main/java/com/karigar/app/data/remote/ApiClient.kt`
> to your PC's LAN IP (e.g. `http://192.168.1.5:4000/api/v1/`).

---

## 📲 App flow

Splash (JWT verify) → Onboarding → Login / Register → Dashboard (services)
→ Confirm location on map → Review & Pay → Order placed.

Bottom navigation: **Home · Orders · History · Profile**.

---

## 🔐 Security notes

- `.env` is never committed. Use `backend/.env.example` as a template.
- `ENCRYPTION_KEY` is the master key for encrypted PII — back it up securely; if lost, encrypted data is unrecoverable.
- The debug APK is for testing only. Publish a signed release build for production.

---

## 📌 Roadmap

- [ ] Firebase phone OTP on the client
- [ ] FCM push notifications for order alerts (works when app is killed)
- [ ] Real payment gateway (UPI / Razorpay)
- [ ] Worker app screens
- [ ] Live worker tracking (WebSocket)

---

Built with ❤️ for local workers and the people who need them.
