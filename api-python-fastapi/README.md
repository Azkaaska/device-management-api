# Device Management API — Python / FastAPI

Implementasi backend IoT Device Management API menggunakan **Python 3** dan **FastAPI**. Dokumen ini berisi panduan lengkap mengenai arsitektur, spesifikasi endpoint API, payload, MQTT, WebSocket, serta instruksi menjalankan aplikasi.

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
| Framework | FastAPI |
| ORM (Perangkat) | SQLAlchemy |
| Database Perangkat | PostgreSQL |
| Database Telemetri | Apache Cassandra (driver python) |
| Broker MQTT | Paho MQTT Client |
| WebSocket | FastAPI WebSocket |
| Dokumentasi API | Swagger UI (FastAPI built-in) |

---

## Prasyarat

Pastikan layanan-layanan berikut sudah berjalan sebelum meluncurkan aplikasi:

- **Python 3.8+**
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
| `DB_POOL_SIZE` | `5` | Ukuran connection pool SQLAlchemy |
| `DB_MAX_OVERFLOW` | `10` | Jumlah maksimal koneksi overflow |
| `DB_POOL_TIMEOUT` | `30` | Timeout koneksi pool (detik) |
| `DB_POOL_RECYCLE` | `1800` | Umur maksimal koneksi sebelum didaur ulang (detik) |
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

1. Buat virtual environment (opsional tapi disarankan):
   ```bash
   python -m venv venv
   source venv/bin/activate  # Unix/Linux
   # atau
   .\venv\Scripts\activate   # Windows Powershell
   ```

2. Instal dependensi:
   ```bash
   pip install -r requirements.txt
   ```

3. Jalankan server FastAPI:
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 3000 --reload
   # atau
   python main.py
   ```

Aplikasi web dan API akan siap melayani permintaan pada `http://localhost:3000`.

---

## Struktur Proyek

```
app/
├── api/
│   ├── routes/
│   │   ├── devices.py             # Endpoint CRUD perangkat
│   │   └── telemetry.py           # Endpoint push & query telemetri
│   └── router.py                  # Pendaftaran rute API v1
├── core/
│   ├── cassandra_db.py            # Helper koneksi Cassandra
│   ├── discord.py                 # Pemicu notifikasi webhook Discord
│   ├── mqtt_worker.py             # MQTT subscriber daemon
│   ├── postgres_db.py             # Helper koneksi SQLAlchemy PostgreSQL
│   └── websocket_manager.py       # Pengelola koneksi WebSocket klien
├── models/
│   ├── device.py                  # Model ORM SQLAlchemy untuk perangkat
│   └── reading.py                 # Model Cassandra telemetri
├── schemas/
│   ├── device.py                  # Validasi skema input/output perangkat (Pydantic)
│   ├── error.py                   # Skema error API
│   └── telemetry.py               # Validasi skema telemetri (Pydantic)
├── templates/
│   └── live_view.html             # UI minimal untuk live dashboard
├── config.py                      # Konfigurasi aplikasi & settings
└── __init__.py

main.py                            # Entrypoint & inisialisasi lifespan FastAPI
requirements.txt                   # Daftar dependensi modul Python
```

---

## Swagger / OpenAPI

Dokumentasi Swagger UI interaktif dapat diakses pada URI berikut ketika server berjalan:

```
http://localhost:3000/docs-ui
```
