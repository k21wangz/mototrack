# 🏍️ MotoTrack Pro: All-in-One Motorcycle Touring & Telemetry

[![Build MotoTrack APK](https://github.com/k21wangz/mototrack/actions/workflows/build-apk.yml/badge.svg)](https://github.com/k21wangz/mototrack/actions/workflows/build-apk.yml)
[![Author](https://img.shields.io/badge/Author-Wawang%20Kurniawan-blue.svg)](https://github.com/k21wangz)
[![Organization](https://img.shields.io/badge/Institution-PT%20BPR%20NBP%2027-green.svg)](https://github.com/k21wangz)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)

**MotoTrack Pro** adalah aplikasi seluler open-source terpadu yang dirancang khusus untuk para pengendara motor dan penggemar touring. Aplikasi ini menggabungkan fitur telemetri canggih (seperti *Pirelli Diablo Super Biker*), pelacak rute GPS offline, logbook garasi perawatan motor, serta interkom suara grup bebas kuota.

---

## 🌟 Fitur Utama

### 1. 📐 Telemetri Sudut Rebah (Lean Angle HUD)
* **Sensor Fusion Presisi**: Menggunakan sensor rotasi hardware Android (`Sensor.TYPE_ROTATION_VECTOR`) yang menggabungkan giroskop dan akselerometer untuk membaca derajat kemiringan motor secara real-time tanpa terdistorsi gaya sentrifugal saat menikung.
* **Rekor Kemiringan**: Mencatat rekor sudut rebah maksimal kiri (*Max Lean Left*) dan kanan (*Max Lean Right*) di setiap sesi touring.
* **Kalibrasi 1-Klik**: Fitur penyetelan titik nol (0°) untuk mengompensasi posisi HP saat terpasang pada *phone holder* stang motor.
* **Speedometer GPS & Akselerasi**: Kecepatan *real-time*, kecepatan rata-rata, dan kecepatan puncak (*top speed*).

### 2. 🗺️ Pelacak Perjalanan & Rute GPS (Trip Tracker)
* **Peta Offline & Hemat Baterai**: Menggunakan OpenStreetMap (OSM) tanpa perlu koneksi internet dan tanpa biaya API.
* **Perekaman Latar Belakang (*Foreground Service*)**: Jejak rute tetap tercatat secara akurat meski layar smartphone dimatikan atau saat membuka aplikasi navigasi lain.
* **Ekspor & Impor GPX/KML**: Bagikan file rute touring ke sesama rekan komunitas motor.

### 3. 🛠️ Garasi & Catatan Servis Motor (Motorcycle Logbook)
* **Sinkronisasi Odometer Otomatis**: Setiap kali sesi touring diselesaikan, jarak tempuh GPS otomatis mengakumulasi total Odometer motor Anda.
* **Pengingat Servis Berkala**: Pemantau interval penggantian oli mesin (misal: tiap 2.500 KM) dan suku cadang (kampas rem, busi, ban, rantai).
* **Catatan BBM & Biaya Perawatan**: Catat histori pengeluaran biaya bensin dan servis berkala dengan rapi.

### 4. 📻 Touring Intercom (Voice Call / Walkie-Talkie)
* **Mode LAN / Hotspot (0 Kuota Internet)**: 
  * Cukup satu motor menyalakan Hotspot Wi-Fi HP, seluruh rombongan yang tersambung ke Wi-Fi tersebut bisa langsung berbicara satu sama lain.
  * Menggunakan streaming audio UDP Multicast (16kHz PCM) dengan latensi ultra-rendah (<50 ms).
  * **100% Bebas Kuota Internet** — solusi ideal saat touring melintasi pegunungan atau pedalaman tanpa sinyal seluler.
* **Push-To-Talk (PTT)**: Tahan tombol untuk berbicara dan lepas untuk mendengarkan, ramah tombol headset bluetooth helm.

---

## 🚀 Alur Kerja Cloud CI/CD (Build APK Tanpa Android Studio)

Aplikasi ini dilengkapi dengan pipeline **GitHub Actions** otomatis. Anda tidak perlu menginstal Android Studio di laptop:

1. Setiap perubahan kode yang di-*push* ke branch `main` akan otomatis dikompilasi oleh GitHub Actions.
2. File installer `.apk` dapat langsung diunduh dari tab **Actions -> Artifacts -> `MotoTrack-Debug-APK`**.

---

## 💻 Struktur Modul Tambahan

```text
src/main/
├── java/de/dennisguse/opentracks/
│   ├── motorcycle/
│   │   ├── MotorcycleProfile.java       # Model data profil motor & odometer
│   │   ├── MaintenanceLog.java          # Model catatan servis & BBM
│   │   ├── MotorcycleGarageManager.java # Manager garasi & sinkronisasi jarak touring
│   │   ├── TouringIntercomManager.java  # Engine komunikasi suara LAN/Hotspot (PTT)
│   │   └── MotorcycleGarageActivity.java# Layar UI Garasi & Intercom
│   └── sensors/
│       └── LeanAngleSensor.java         # Sensor kemiringan sudut rebah motor
└── res/
    └── layout/
        └── activity_motorcycle_garage.xml # Layout antarmuka Garasi & Intercom
```

---

## 👤 Pengembang & Hak Cipta

* **Author**: Wawang Kurniawan
* **Institusi / Perusahaan**: PT BPR NBP 27
* **Email**: [k21wangz@gmail.com](mailto:k21wangz@gmail.com) / [admin@nbp27.com](mailto:admin@nbp27.com)
* **GitHub**: [@k21wangz](https://github.com/k21wangz)
* **Gitea**: [git.myflix.my.id/wawang](https://git.myflix.my.id/wawang)

---

## 📄 Lisensi

Proyek ini dibangun di atas fondasi open-source [OpenTracks](https://github.com/OpenTracksApp/OpenTracks) dan didistribusikan di bawah lisensi **Apache License 2.0**.

&copy; 2026 Wawang Kurniawan • PT BPR NBP 27
