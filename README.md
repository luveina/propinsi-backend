# Propinsi Backend

Backend application untuk project Propinsi menggunakan Spring Boot.

## Prerequisites

Sebelum menjalankan aplikasi ini, pastikan Anda telah menginstall:

- **Java Development Kit (JDK) 17** atau lebih tinggi
  - Download dari: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
  - Atau gunakan OpenJDK: https://adoptium.net/
- **Maven** (Opsional, karena project sudah include Maven Wrapper)
- **Git** untuk clone repository
- **IDE** (Opsional): IntelliJ IDEA, Eclipse, atau VS Code

## Cara Mengecek Versi Java

```bash
java -version
```

Pastikan output menunjukkan Java 17 atau lebih tinggi.

## Instalasi dan Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd propinsi-backend
```

### 2. Build Project

Untuk Windows (PowerShell/CMD):
```bash
.\mvnw.cmd clean install
```

Proses build akan:
- Download semua dependencies yang dibutuhkan
- Compile source code
- Menjalankan unit tests
- Package aplikasi menjadi file JAR

### 3. Menjalankan Aplikasi

Untuk Windows (PowerShell/CMD):
```bash
.\mvnw.cmd spring-boot:run
```


Aplikasi akan berjalan di `http://localhost:8080`

### 4. Verifikasi Aplikasi Berjalan

Buka browser dan akses:
```
http://localhost:8080
```

Atau gunakan curl:
```bash
curl http://localhost:8080
```

## Struktur Project (Standard, bisa disesuaikan dengan kebutuhan)

```
propinsi-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/propinsi/backend/
│   │   │       └── repository/
│   │   │       └── service/
│   │   │       └── controller/
│   │   │       └── BackendApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/propinsi/backend/
│               └── BackendApplicationTests.java
├── target/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Konfigurasi

File konfigurasi utama terletak di `src/main/resources/application.properties`.

### Mengubah Port Server

Edit `application.properties` dan tambahkan:
```properties
server.port=8081
```

## Menjalankan Tests

```bash
.\mvnw.cmd test
```

## Build untuk Production

Build aplikasi menjadi JAR file:
```bash
.\mvnw.cmd clean package
```

JAR file akan dibuat di `target/backend-0.0.1-SNAPSHOT.jar`

Jalankan JAR file:
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Java Version Mismatch

**Error:**
```
error: release version 25 not supported
```

**Solusi:** Pastikan `pom.xml` menggunakan Java version yang sesuai dengan JDK terinstall
```xml
<properties>
    <java.version>17</java.version>
</properties>
```

### Build Gagal - Dependencies Error

**Error:**
```
Could not resolve dependencies
```

**Solusi:**
1. Pastikan koneksi internet stabil
2. Clear Maven cache:
   ```bash
   .\mvnw.cmd clean
   ```
3. Delete folder `.m2/repository` di home directory dan build ulang

### Class Not Found Error

**Error:**
```
Error: Could not find or load main class com.propinsi.backend.BackendApplication
```

**Solusi:** Build ulang project
```bash
.\mvnw.cmd clean install
```

## Development

### Hot Reload

Project ini sudah include Spring DevTools untuk hot reload. Perubahan pada code akan otomatis di-reload tanpa perlu restart aplikasi.

### Menambah Dependencies

Edit `pom.xml` dan tambahkan dependency yang dibutuhkan, kemudian jalankan:
```bash
.\mvnw.cmd clean install
```

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.2**
- **Spring Web MVC**
- **Spring DevTools** (untuk development)
- **Lombok** (untuk mengurangi boilerplate code)
- **Maven** (build tool)

## Git Workflow (Standard, bisa disesuaikan dengan kebutuhan)

```
main (production)
├── staging (pre-production)
│   ├── feature/nama-fitur-1
│   ├── feature/nama-fitur-2
│   └── fix/nama-bug
```

## 1. Manajemen Akun & Autentikasi (EPIC 01)
Berikut adalah daftar fitur utama yang dikerjakan berdasarkan Product Backlog Item (PBI) SILOBUR-NG:

- PBI-1: Registrasi Peserta
Implementasi endpoint POST /api/auth/register untuk pendaftaran mandiri pengguna (Peserta).
- PBI-2: Login
Implementasi endpoint POST /api/auth/login dengan validasi kredensial dan status akun aktif.
- PBI-3: Logout Pengguna
Implementasi endpoint POST /api/auth/logout untuk proses invalidasi token sesi (security).
- PBI-17: [C/U] Upload Bukti Pembayaran & Locking Seat
Implementasi logic POST /api/reservasi/upload-bukti termasuk mekanisme penguncian nomor gantangan di database untuk menghindari race condition.
- PBI-21: [R/U] Interactive Gantangan & Selection
Implementasi fitur interaktif bagi Juri untuk melihat teknis gantangan dan melakukan input seleksi burung terbaik melalui POST /api/scoring/vote.

## 2. Manajemen Lomba (EPIC 02)
Berikut adalah detail implementasi BE berdasarkan Product Backlog Item (PBI) SILOBUR-NG:
- **PBI-4 dan 5**: Endpoint GET /api/profile untuk melihat data diri , PUT /api/profile/password untuk update password , dan PUT /api/accounts/{id} untuk update data akun oleh Admin.
- **PBI-17**: Endpoint POST /api/reservasi/upload-bukti dengan logic re-check status seat di database untuk menghindari race condition (Conflict 409).
- **PBI-22**: Klasemen Semi-Final & Waiting Room – Rekapitulasi GET /api/scoring/semi-final dan penentuan skor akhir (Poin Koncer A/B) melalui POST /api/scoring/koncer.


## 3. Monitoring Pendaftaran & Pembayaran (EPIC 03)

- PBI-16 (Reservasi): Endpoint GET /api/reservasi/denah/{lomba_id} untuk mengecek status ketersediaan nomor gantangan (Available/Booked).

- PBI-17 (Upload Bukti): Endpoint POST /api/reservasi/upload-bukti dengan logic re-check status seat di database untuk menghindari race condition (Conflict 409).

- PBI-18 & PBI-19 (Verifikasi): Endpoint PATCH /api/reservasi/verify/{id} bagi Koordinator Pendaftaran untuk mengubah status menjadi 'Paid' atau 'Invalid' , serta GET /api/profile/my-tickets bagi peserta untuk melihat E-Ticket.

## 4. Sistem Penjurian (EPIC 04)

Berikut adalah daftar **Product Backlog Item (PBI)** yang sedang dan akan saya kerjakan:

- [ ] **[R] Katalog Lomba** Fitur untuk menampilkan daftar lomba yang tersedia bagi peserta.
- [ ] **[R] Detail Lomba** Halaman untuk melihat informasi lengkap mengenai suatu lomba.
- [ ] **[R/U] Verifikasi Pembayaran Peserta** Fitur admin untuk melihat bukti bayar dan melakukan validasi (terima/tolak).
- [ ] **[U] Detail Lomba Juri** Fitur khusus Juri untuk memperbarui atau melengkapi data pada detail lomba.
- [ ] **[R] Pengumuman Hasil Akhir Lomba** Halaman untuk menampilkan hasil akhir atau pemenang lomba.
- [ ] **[R] Daftar Partisipan Lomba** Fitur untuk melihat daftar peserta yang sudah terdaftar di lomba tertentu.

*(Keterangan: [R] = Read/View, [U] = Update/Edit)*

## 5. Monitoring Statistik Lomba (EPIC 05)

- PBI-25 & PBI-26 (Operasional Lapangan): Fitur daftar partisipan per kelas GET /api/events/{eventId}/participants dan update status kehadiran via PATCH /api/participants/{participantId}/check-in.

- PBI-27 & PBI-28 (Analytics): Dashboard ringkasan GET /api/dashboard/summary untuk menghitung Revenue, Occupancy, dan Attendance Rate , serta GET /api/dashboard/analytics untuk tren penjualan harian.
