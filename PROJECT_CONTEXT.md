# PROJECT_CONTEXT.md

> AI Development Context for **AGUNG ANAMIROH**
>
> **Version:** 1.0
> **Status:** Active Development
> **Architecture:** MVVM
> **Target Platform:** Android Native
> **Primary Language:** Kotlin
> **UI Framework:** Jetpack Compose

---

# 1. PROJECT OVERVIEW

## Project Name

AGUNG ANAMIROH

## Project Type

Professional Android Application

## Business Domain

Umrah Travel Management System

## Development Status

Active Development

## Objective

Membangun aplikasi Android profesional untuk perusahaan travel umrah yang mampu mengelola seluruh proses operasional mulai dari pendaftaran jamaah hingga laporan administrasi secara digital.

Project ini **bukan project latihan**.

Seluruh implementasi harus memenuhi standar aplikasi production.

---

# 2. PROJECT VISION

Project ini harus berkembang menjadi aplikasi enterprise yang:

- scalable
- maintainable
- reusable
- secure
- responsive
- production ready

Semua keputusan teknis harus mengutamakan kualitas arsitektur dibanding kecepatan implementasi.

---

# 3. DEVELOPMENT PRINCIPLES

Semua AI yang membantu project ini WAJIB mengikuti prinsip berikut.

## DO

✔ Gunakan MVVM

✔ Gunakan Repository Pattern

✔ Gunakan StateFlow

✔ Gunakan Kotlin Coroutines

✔ Gunakan Material 3

✔ Gunakan Jetpack Compose

✔ Gunakan reusable components

✔ Gunakan clean architecture

✔ Tulis kode production ready

✔ Berikan analisis sebelum coding

✔ Pertahankan konsistensi project

---

## DON'T

✘ Jangan menggunakan XML

✘ Jangan menggunakan Fragment

✘ Jangan membuat duplicate ViewModel

✘ Jangan membuat duplicate Repository

✘ Jangan membuat duplicate Data Class

✘ Jangan mengubah package project

✘ Jangan menghapus fitur yang sudah selesai

✘ Jangan membuat business logic di UI

✘ Jangan membuat screen terlalu panjang

✘ Jangan mengubah struktur project tanpa alasan teknis

---

# 4. TECHNOLOGY STACK

Programming Language

- Kotlin

UI

- Jetpack Compose
- Material 3

Architecture

- MVVM
- Repository Pattern

Database

- Cloud Firestore

Authentication

- Firebase Authentication

State Management

- StateFlow

Async

- Kotlin Coroutines

Navigation

- Navigation Compose

Export

- Apache POI

Document

- PDF Generator

---

# 5. PROJECT STRUCTURE

```
app/

data/

model/

repository/

navigation/

ui/

component/

screen/

auth/

agent/

admin/

theme/

utils/

viewmodel/
```

Struktur project harus tetap sederhana namun mudah dikembangkan.

---

# 6. DESIGN SYSTEM

## Theme

Luxury

Modern

Minimalist

Enterprise

Professional

## Primary Color

Gold

```
#D4AF37
```

## Background

White

```
#FFFFFF
```

## Secondary

Light Gray

## Text

Black

## Radius

16dp - 24dp

## Card

Material 3

Rounded

Soft Shadow

## Animation

Simple

Smooth

Professional

---

# 7. USER ROLES

## ADMIN

Hak akses penuh.

### Menu

Dashboard

Kelola Paket

Data Jamaah

Approval Pembayaran

Invoice

Export Excel

Report

Monitoring Agent

Notification

---

## AGENT

Hanya dapat mengakses data miliknya sendiri.

### Menu

Dashboard

Input Jamaah

Data Jamaah

Pembayaran

Invoice

Riwayat

Profil

---

# 8. FIREBASE

Authentication

Firebase Authentication

Database

Cloud Firestore

---

# 9. FIRESTORE COLLECTIONS

```
users

jamaah

paket_umroh

pembayaran

invoice

counters
```

Collection dapat bertambah sesuai kebutuhan project.

---

# 10. USER COLLECTION

```
users

uid

nama

email

role

status
```

Role

```
admin

agent
```

---

# 11. AUTH FLOW

```
Splash

↓

Login

↓

Firebase Authentication

↓

Firestore Users

↓

Read Role

↓

Admin

↓

Admin Dashboard

OR

↓

Agent

↓

Agent Dashboard
```

---

# 12. CURRENT DEVELOPMENT STATUS

## COMPLETED

Authentication

Login Screen

Role Based Login

Navigation

Theme

Agent Dashboard

---

## IN PROGRESS

Input Jamaah

---

## NEXT

Data Jamaah

Detail Jamaah

Edit Jamaah

Pembayaran

Invoice

Export

Admin Dashboard

---

# 13. DATA MODEL

## Jamaah

```
id

nama

program

keberangkatan

noHp

alamat

gender

binBinti

tempatLahir

tanggalLahir

noPaspor

dp

agentId

status
```

---

# 14. DEVELOPMENT ROADMAP

Phase 1

Authentication

✅

Phase 2

Agent Dashboard

✅

Phase 3

Input Jamaah

🚧

Phase 4

Data Jamaah

Phase 5

Detail Jamaah

Phase 6

Edit Jamaah

Phase 7

Pembayaran

Phase 8

Invoice

Phase 9

Export

Phase 10

Admin Dashboard

Phase 11

Kelola Paket

Phase 12

Laporan

Phase 13

Notification

Phase 14

Production Release

---

# 15. DEVELOPMENT WORKFLOW

Sebelum membuat kode:

## Step 1

Analisis kebutuhan.

## Step 2

Analisis struktur project.

## Step 3

Analisis dependency.

## Step 4

Cari apakah class serupa sudah ada.

## Step 5

Pastikan tidak membuat duplicate.

## Step 6

Rancang solusi.

## Step 7

Implementasi.

## Step 8

Self review.

## Step 9

Pastikan compile tanpa error.

Baru kemudian tampilkan hasil.

---

# 16. CODE QUALITY CHECKLIST

Sebelum memberikan jawaban, AI wajib memastikan:

☐ Tidak ada duplicate code

☐ Tidak ada duplicate ViewModel

☐ Tidak ada duplicate Repository

☐ Tidak ada duplicate Model

☐ Import sudah benar

☐ Compatible dengan project

☐ Compile tanpa error

☐ Mengikuti Material 3

☐ Mengikuti MVVM

☐ Reusable

☐ Mudah di-maintain

---

# 17. RESPONSE FORMAT FOR AI

Setiap kali diminta membuat fitur baru, AI WAJIB memberikan urutan berikut:

1. Analisis kebutuhan

2. Analisis arsitektur

3. File yang akan dibuat

4. File yang akan dimodifikasi

5. Penjelasan dependency

6. Implementasi lengkap

7. Cara integrasi

8. Checklist testing

Jangan langsung menghasilkan kode tanpa analisis.

---

# 18. LONG TERM GOAL

Target akhir project adalah aplikasi Android enterprise yang siap digunakan oleh perusahaan travel umrah.

Fokus utama:

- Clean Architecture
- High Performance
- Maintainability
- Scalability
- Security
- Professional UI/UX
- Production Ready

Semua keputusan teknis harus mengarah pada tujuan tersebut.

---

# 19. AI ROLE

Setiap AI yang membaca file ini harus bertindak sebagai:

- Senior Android Engineer
- Software Architect
- Firebase Engineer
- Kotlin Expert
- Jetpack Compose Expert
- UI/UX Consultant
- Code Reviewer
- Technical Lead

AI tidak hanya menghasilkan kode, tetapi juga bertanggung jawab menjaga kualitas arsitektur project.

---

# END OF CONTEXT
