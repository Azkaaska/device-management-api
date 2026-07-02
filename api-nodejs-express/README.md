# Device Management API — Node.js / Express

Implementasi backend IoT Device Management API menggunakan **Node.js** dan **Express**. Dokumen ini berisi panduan lengkap mengenai arsitektur, spesifikasi endpoint API, payload, MQTT, WebSocket, serta instruksi menjalankan aplikasi.

---

## Arsitektur & Konvensi API

Proyek ini dirancang untuk menangani metadata perangkat keras (IoT) dan ingesti telemetri deret waktu (time-series).

| Aspek | Detail |
|---|---|
| **Database Perangkat** | PostgreSQL — menyimpan metadata perangkat (`devices`) dan konfigurasi status operasional |
| **Database Telemetri** | Apache Cassandra — menyimpan data time-series dengan partisi harian (`bucket_date`) |
| **Broker MQTT** | Eclipse Mosquitto — aplikasi berjalan sebagai MQTT subscriber internal untuk ingesti data sensor |
| **Device ID** | UUID v4 yang dibuat secara otomatis oleh PostgreSQL |
| **Timestamp** | Disimpan dan dikirim sebagai UNIX timestamp dalam milidetik (epoch ms) |
| **Port Default** | Server berjalan secara default pada `http://localhost:3000` |

---

## Endpoint API

### Perangkat (Devices)

| Method | Path | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/devices` | Ambil daftar semua perangkat (paginasi: `?page=0&limit=20`) |
| `POST` | `/api/v1/devices` | Daftarkan perangkat baru |
| `GET` | `/api/v1/devices/{id}` | Ambil detail perangkat berdasarkan ID |
| `PUT` | `/api/v1/devices/{id}` | Perbarui data metadata perangkat |
| `DELETE` | `/api/v1/devices/{id}` | Hapus perangkat (soft delete — status berubah menjadi `inactive`) |

### Telemetri

| Method | Path | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/devices/{id}/telemetry` | Ambil telemetri terbaru (atau riwayat jika `?start_time` & `?end_time` disertakan) |
| `POST` | `/api/v1/devices/{id}/telemetry` | Kirim satu data pembacaan telemetri untuk perangkat |

> **Catatan query historis:** Parameter `start_time` dan `end_time` adalah UNIX timestamp dalam milidetik. Contoh:
> `GET /api/v1/devices/{id}/telemetry?start_time=1717200000000&end_time=1717286400000&page=0&limit=20`

### WebSocket (Live Dashboard)

Server mengekspos koneksi WebSocket real-time pada URI:

```
ws://localhost:3000/api/ws
```

Klien dapat memfilter aliran data dengan menyertakan query parameter `device_id` pada jabat tangan WebSocket:
- `ws://localhost:3000/api/ws?device_id=550e8400-e29b-41d4-a716-446655440000` (Hanya menerima data dari ID perangkat tersebut).
- `ws://localhost:3000/api/ws` (Menerima data telemetri dari semua perangkat).

Format payload pesan real-time yang dikirimkan ke klien:

```json
{ "type": "READING", "payload": { ... } }
{ "type": "ALERT",   "payload": { ... } }
```

Event `ALERT` secara otomatis disiarkan apabila sensor mendeteksi suhu melebihi ambang batas (**35°C**).

---

## Format Payload

### Body `POST /api/v1/devices`

```json
{
  "name": "Sensor Suhu Ruang Server",
  "type": "Thermometer",
  "status": "active"
}
```

### Body `POST /api/v1/devices/{id}/telemetry`

```json
{
  "ts": 1717488000000,
  "temperature": 28.5,
  "humidity": 75.2
}
```

---

## Format Error

Semua error dari server dikembalikan dalam struktur JSON standar:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Device not found",
  "path": "/api/v1/devices/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1717488000000
}
```

---

## Notifikasi Discord Webhook

Apabila perangkat baru berhasil didaftarkan via REST API, aplikasi akan mengirimkan notifikasi berupa Rich Embed ke saluran Discord menggunakan URL webhook yang dikonfigurasi melalui variabel lingkungan `DISCORD_WEBHOOK_URL`.

---

## Ingesti Data MQTT

Subscriber internal mendengarkan pesan MQTT dari sensor (emulator) dengan format topik:

```
buildingA/{ruangan}/{device-uuid}
```

Contoh: `buildingA/room1/550e8400-e29b-41d4-a716-446655440000`  
Payload pesan MQTT mengikuti format payload telemetri yang sama dengan endpoint HTTP POST di atas.

---

## Teknologi

| Komponen | Teknologi |
|---|---|
| Framework | Express |
| ORM (Perangkat) | Sequelize (dengan Driver pg) |
| Database Perangkat | PostgreSQL |
| Database Telemetri | Apache Cassandra (driver cassandra-driver) |
| Broker MQTT | MQTT.js |
| WebSocket | ws |
| Dokumentasi API | swagger-ui-express + yamljs |

---

## Prasyarat

Pastikan layanan-layanan berikut sudah berjalan sebelum meluncurkan aplikasi:

- **Node.js 18+**
- **PostgreSQL** — database relasional untuk metadata perangkat
- **Apache Cassandra** — database deret waktu untuk telemetri
- **MQTT Broker** (mis. Eclipse Mosquitto) — untuk broker pengiriman pesan sensor

---

## Konfigurasi Environment

Salin file `.env.example` menjadi `.env` dan isi dengan konfigurasi Anda:

```bash
cp .env.example .env
```

| Variabel | Default | Keterangan |
|---|---|---|
| `DATABASE_URL` | `postgresql://user:password@localhost:5432/database` | URL koneksi database PostgreSQL |
| `DISCORD_WEBHOOK_URL` | _(kosong)_ | URL webhook Discord untuk notifikasi (opsional) |
| `DB_POOL_MAX` | `5` | Jumlah maksimal koneksi pool Sequelize |
| `DB_POOL_MIN` | `0` | Jumlah minimal koneksi pool Sequelize |
| `DB_POOL_ACQUIRE` | `30000` | Batas waktu timeout mencoba mengambil koneksi (ms) |
| `DB_POOL_IDLE` | `10000` | Batas waktu idle koneksi sebelum dilepas (ms) |
| `CASSANDRA_CONTACT_POINTS` | `127.0.0.1` | Host Cassandra |
| `CASSANDRA_PORT` | `9042` | Port Cassandra |
| `CASSANDRA_KEYSPACE` | `keyspace` | Nama keyspace Cassandra |
| `CASSANDRA_LOCAL_DC` | `datacenter` | Nama datacenter lokal Cassandra |
| `CASSANDRA_USER` | `user` | Username Cassandra |
| `CASSANDRA_PASSWORD` | `password` | Password Cassandra |
| `MQTT_HOST` | `127.0.0.1` | Host broker MQTT |
| `MQTT_PORT` | `1883` | Port broker MQTT |

---

## Cara Menjalankan Aplikasi

1. Instal dependensi npm:
   ```bash
   npm install
   ```

2. Jalankan server Node.js:
   ```bash
   # Jalankan dalam mode development (dengan reload otomatis jika kode berubah)
   npm run dev

   # Jalankan dalam mode production
   npm start
   ```

Aplikasi web dan API akan siap melayani permintaan pada `http://localhost:3000`.

---

## Struktur Proyek

```
src/
├── config/
│   ├── cassandra.js               # Pengaturan koneksi Cassandra
│   └── postgres.js                # Pengaturan koneksi Sequelize PostgreSQL
├── controllers/
│   ├── deviceController.js        # Controller logika HTTP perangkat
│   └── telemetryController.js     # Controller logika HTTP telemetri
├── middlewares/
│   ├── errorHandler.js            # Middleware penanganan error terpusat
│   └── validator.js               # Middleware validasi data request
├── models/
│   ├── Device.js                  # Skema model perangkat (Sequelize)
│   ├── Reading.js                 # Skema model telemetri (Cassandra)
│   └── index.js                   # Ekspor gabungan model
├── public/
│   └── index.html                 # UI sederhana live dashboard
├── routes/
│   ├── devices.js                 # Rute API untuk perangkat
│   ├── index.js                   # Registrasi rute global
│   └── telemetry.js               # Rute API untuk telemetri
├── services/
│   ├── deviceService.js           # Layanan logika database perangkat
│   ├── telemetryService.js        # Layanan logika database telemetri
│   └── websocketService.js        # Pengelola broadcast WebSocket
├── utils/
│   ├── discordWebhook.js          # Helper notifikasi webhook Discord
│   └── eventHub.js                # Event emitter terpusat untuk komunikasi real-time
└── workers/
    └── mqttIngestion.js           # Worker penangkap data sensor MQTT

index.js                           # Entrypoint aplikasi Express & WebSocket
swagger.yaml                       # Spesifikasi Swagger OpenAPI 3
package.json                       # Daftar script dan dependensi modul Node.js
```

---

## Swagger / OpenAPI

Dokumentasi Swagger UI interaktif dapat diakses pada URI berikut ketika server berjalan:

```
http://localhost:3000/docs-ui
```
