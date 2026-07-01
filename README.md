# AGUNG ANAMIROH
### Professional Umrah Travel Management System

> Modern Android application for managing Umrah travel operations with Firebase, Jetpack Compose, and Clean MVVM Architecture.

---

<p align="center">

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange?style=for-the-badge)
![Backend](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase)

</p>

---

# 📖 About Project

**AGUNG ANAMIROH** adalah aplikasi Android Native yang dikembangkan untuk membantu perusahaan travel umrah dalam mengelola seluruh proses operasional secara digital.

Project ini dibangun menggunakan arsitektur modern sehingga mudah dikembangkan, mudah dipelihara, dan siap digunakan pada lingkungan production.

Project ini **bukan project pembelajaran**, melainkan ditujukan sebagai aplikasi profesional yang dapat digunakan oleh perusahaan travel umrah.

---

# 🎯 Main Goals

- Digitalisasi proses administrasi travel umrah
- Mempermudah pekerjaan Agent dan Admin
- Manajemen data jamaah secara real-time
- Manajemen paket umrah
- Sistem pembayaran
- Pembuatan Invoice
- Export laporan
- Generate PDF
- Dashboard profesional
- Siap dikembangkan menjadi aplikasi enterprise

---

# ✨ Features

## 🔐 Authentication

- Firebase Authentication
- Login
- Logout
- Role Based Login
- Admin
- Agent

---

## 👤 Agent Features

- Dashboard
- Input Jamaah
- Data Jamaah
- Detail Jamaah
- Edit Jamaah
- Pembayaran
- Invoice
- Riwayat Transaksi
- Profil

---

## 👨‍💼 Admin Features

- Dashboard
- Kelola Paket Umrah
- Data Seluruh Jamaah
- Approval Pembayaran
- Export Excel
- Generate PDF
- Invoice Management
- Statistik
- Monitoring Agent

---

# 🏗 Technology Stack

| Technology | Description |
|------------|-------------|
| Kotlin | Programming Language |
| Jetpack Compose | Modern Android UI |
| Material 3 | Design System |
| Firebase Authentication | User Authentication |
| Cloud Firestore | Database |
| Navigation Compose | Navigation |
| MVVM | Architecture Pattern |
| Repository Pattern | Data Layer |
| StateFlow | UI State Management |
| Kotlin Coroutines | Asynchronous Programming |
| Apache POI | Excel Export |
| PDF Generator | Invoice & Report |

---

# 🏛 Architecture

Project menggunakan pola arsitektur **MVVM (Model View ViewModel)**.

```
UI (Compose)
      │
      ▼
 ViewModel
      │
      ▼
 Repository
      │
      ▼
 Firebase
      │
      ▼
 Firestore
```

Seluruh business logic berada pada **Repository** dan **ViewModel**, sedangkan UI hanya bertugas menampilkan data.

---

# 📁 Project Structure

```
app/
│
├── data/
│   ├── model/
│   └── repository/
│
├── navigation/
│
├── ui/
│   ├── component/
│   ├── screen/
│   │     ├── auth/
│   │     ├── admin/
│   │     └── agent/
│   └── theme/
│
├── utils/
│
└── viewmodel/
```

---

# ☁ Firebase Collections

Project menggunakan Cloud Firestore.

```
users

jamaah

paket_umroh

pembayaran

invoice

counters
```

---

# 👥 User Roles

## Admin

Memiliki akses penuh terhadap seluruh sistem.

### Hak Akses

- Kelola Paket
- Kelola Jamaah
- Approval Pembayaran
- Dashboard
- Export Excel
- Generate PDF
- Monitoring Agent
- Laporan

---

## Agent

Memiliki akses terhadap data miliknya sendiri.

### Hak Akses

- Dashboard
- Input Jamaah
- Kelola Jamaah
- Pembayaran
- Invoice
- Profil

---

# 🎨 Design System

### Primary Color

Gold

```
#D4AF37
```

### Background

White

```
#FFFFFF
```

### Accent

Light Gray

### Text

Black

---

### UI Style

- Modern
- Professional
- Enterprise
- Luxury
- Elegant
- Minimalist
- Responsive
- Material 3

---

# 🚀 Development Roadmap

| Module | Status |
|---------|--------|
| Authentication | ✅ Done |
| Login Screen | ✅ Done |
| Role Based Login | ✅ Done |
| Agent Dashboard | ✅ Done |
| Input Jamaah | 🚧 In Progress |
| Data Jamaah | ⏳ Planned |
| Detail Jamaah | ⏳ Planned |
| Edit Jamaah | ⏳ Planned |
| Pembayaran | ⏳ Planned |
| Invoice | ⏳ Planned |
| Export Excel | ⏳ Planned |
| Admin Dashboard | ⏳ Planned |
| Kelola Paket | ⏳ Planned |
| Report | ⏳ Planned |
| Notification | ⏳ Planned |

---

# ⚙ Development Environment

| Item | Version |
|------|---------|
| Android Studio | Latest Stable |
| VS Code | Latest |
| JDK | 17 |
| Gradle | Latest |
| Android SDK | API 36 |

---

# 📌 Coding Standards

- Kotlin Only
- Jetpack Compose Only
- Material 3
- MVVM
- Repository Pattern
- StateFlow
- Coroutines
- Clean Code
- Reusable Components
- Production Ready

---

# 🤝 Contributing

Seluruh perubahan harus mengikuti standar berikut:

- Tidak membuat duplicate class
- Tidak membuat duplicate ViewModel
- Tidak membuat duplicate Repository
- Tidak mengubah package project
- Mengikuti arsitektur MVVM
- Menjaga konsistensi UI
- Seluruh kode harus dapat di-compile tanpa error

---

# 📄 License

This project is developed for **AGUNG ANAMIROH**.

All rights reserved.

---

<p align="center">

**AGUNG ANAMIROH**

*Professional Umrah Travel Management System*

Built with ❤️ using Kotlin, Jetpack Compose & Firebase.

</p>
