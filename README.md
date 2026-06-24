# IoT Device Management API

Repositori ini berisi implementasi backend untuk IoT Device Management API, ditulis dalam tiga bahasa/framework yang berbeda untuk mendemonstrasikan arsitektur, routing, dan perilaku yang identik di berbagai teknologi.

## Implementasi

| Bahasa / Framework | Direktori |
|---|---|
| Node.js (Express) | `/api-nodejs-express` |
| Python (FastAPI) | `/api-python-fastapi` |
| Java (Spring Boot) | `/api-java-springboot` |

---

## Arsitektur & Konvensi Bersama

Ketiga implementasi berbagi skema database dan konvensi routing yang sama persis.

| Aspek | Detail |
|---|---|
| **Database Perangkat** | PostgreSQL — menyimpan metadata perangkat (`devices`) dan konfigurasi webhook (`webhook_configs`) |
| **Database Telemetri** | Apache Cassandra — menyimpan data time-series dengan partisi per hari (`bucket_date`) |
| **Broker MQTT** | Eclipse Mosquitto — setiap implementasi menjalankan MQTT subscriber internal |
| **Device ID** | UUID v4 yang dibuat secara otomatis oleh database |
| **Timestamp** | Disimpan dan dikembalikan sebagai integer UNIX dalam milidetik |
| **Port Default** | Semua server berjalan di `http://localhost:3000` |

---

## Endpoint API

### Perangkat (Devices)

| Method | Path | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/devices` | Ambil semua perangkat (paginasi: `?page=0&limit=20`) |
| `POST` | `/api/v1/devices` | Daftarkan perangkat baru |
| `GET` | `/api/v1/devices/{id}` | Ambil detail perangkat berdasarkan ID |
| `PUT` | `/api/v1/devices/{id}` | Perbarui data perangkat |
| `DELETE` | `/api/v1/devices/{id}` | Hapus perangkat (soft delete — status menjadi `inactive`) |

### Telemetri

| Method | Path | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/devices/{id}/telemetry` | Ambil data telemetri terbaru (atau historis jika `?start_time` & `?end_time` disertakan) |
| `POST` | `/api/v1/devices/{id}/telemetry` | Push satu data telemetri untuk perangkat |

> **Catatan query historis:** Parameter `start_time` dan `end_time` adalah UNIX timestamp dalam milidetik. Contoh:
> `GET /api/v1/devices/{id}/telemetry?start_time=1717200000000&end_time=1717286400000&page=0&limit=20`

### WebSocket (Live Dashboard)

Semua implementasi mengekspos endpoint WebSocket di:

```
ws://localhost:3000/api/ws
```

Klien menerima event JSON secara real-time dengan format:

```json
{ "type": "READING", "payload": { ... } }
{ "type": "ALERT",   "payload": { ... } }
```

Event `ALERT` dikirim secara otomatis ketika suhu melebihi ambang batas yang dikonfigurasi (default: **35°C**).

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

Semua error dikembalikan dalam format JSON yang konsisten:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Device not found",
  "path": "/api/v1/devices/invalid-id",
  "timestamp": 1717488000000
}
```

---

## Notifikasi Discord Webhook

Ketika perangkat baru didaftarkan, semua implementasi mengirim notifikasi embed ke Discord webhook yang dikonfigurasi. URL webhook diatur melalui variabel lingkungan `DISCORD_WEBHOOK_URL`.

---

## Testing & Dokumentasi

- **Postman Collection:** File `postman_collection.json` tersedia di direktori root. Impor ke Postman untuk langsung menguji ketiga server (UUID perangkat diinjeksi secara dinamis antar request).
- **Swagger / OpenAPI UI:** Tersedia di semua implementasi pada `http://localhost:3000/docs-ui`.

---

## Topik MQTT

Setiap implementasi berlangganan ke topik dengan pola:

```
buildingA/{ruangan}/{device-uuid}
```

Contoh: `buildingA/room1/550e8400-e29b-41d4-a716-446655440000`

Payload pesan mengikuti format telemetri yang sama seperti endpoint HTTP di atas.
