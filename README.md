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