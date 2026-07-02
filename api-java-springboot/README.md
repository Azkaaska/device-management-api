# Device Management API — Java / Spring Boot

Implementasi backend IoT Device Management API menggunakan **Java 25** dan **Spring Boot 4**. Dokumen ini berisi panduan lengkap mengenai arsitektur, spesifikasi endpoint API, payload, MQTT, WebSocket, serta instruksi menjalankan aplikasi dan pengujian.

---

## Arsitektur & Konvensi API

Proyek ini dirancang untuk menangani metadata perangkat keras (IoT) dan ingesti telemetri deret waktu (time-series).

| Aspek | Detail |
|---|---|
| **Database Perangkat** | PostgreSQL — menyimpan metadata perangkat (`devices`) dan konfigurasi status operasional |
| **Database Telemetri** | Apache Cassandra — menyimpan data time-series dengan partisi harian (`bucket_date`) |
| **Broker MQTT** | Eclipse Mosquitto — aplikasi terhubung ke MQTT subscriber internal |
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

Semua error dari server dikembalikan dalam struktur JSON standar (`ApiErrorResponse`):

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Perangkat tidak ditemukan",
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
Payload pesan MQTT mengikuti format payload telemetri (`ReadingInput`) yang sama dengan endpoint HTTP POST di atas.

---

## Teknologi

| Komponen | Teknologi |
|---|---|
| Framework | Spring Boot 4 |
| ORM (Perangkat) | Spring Data JPA + Hibernate |
| Database Perangkat | PostgreSQL |
| Database Telemetri | Apache Cassandra |
| Broker MQTT | Eclipse Paho Client (MQTT v3) |
| WebSocket | Spring WebSocket (`TextWebSocketHandler`) |
| Dokumentasi API | SpringDoc OpenAPI 3 (Swagger UI) |
| Testing | JUnit 5 + Mockito + Spring MockMvc |

---

## Prasyarat

Pastikan layanan-layanan berikut sudah berjalan sebelum meluncurkan aplikasi:

- **JDK 25+**
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
| `DATABASE_DB` | `database` | Nama database PostgreSQL |
| `DATABASE_USER` | `user` | Username PostgreSQL |
| `DATABASE_PASSWORD` | `password` | Password PostgreSQL |
| `DISCORD_WEBHOOK_URL` | _(kosong)_ | URL webhook Discord untuk notifikasi (opsional) |
| `DB_POOL_MAX` | `15` | Ukuran maksimum connection pool HikariCP |
| `DB_POOL_MIN` | `5` | Ukuran minimum idle connection pool |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000` | Timeout koneksi pool (ms) |
| `DB_POOL_IDLE_TIMEOUT` | `600000` | Timeout idle connection (ms) |
| `DB_POOL_MAX_LIFETIME` | `1800000` | Masa hidup maksimum koneksi (ms) |
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

```bash
# Jalankan dalam mode development
.\mvnw.cmd spring-boot:run

# Atau build paket JAR kemudian jalankan
.\mvnw.cmd package -DskipTests
java -jar target/deviceapi-0.0.1-SNAPSHOT.jar

# Gunakan ./mvnw pada shell berbasis unix/linux
```

Aplikasi web dan API akan siap melayani permintaan pada `http://localhost:3000`.

---

## Menjalankan Unit Test

Aplikasi ini dilengkapi dengan **66 unit test** komprehensif yang menguji seluruh lapisan:

```bash
.\mvnw.cmd test

# Gunakan ./mvnw pada shell berbasis unix/linux
```

| Kelas Test | Jumlah Test | Cakupan |
|---|---|---|
| `DeviceServiceTest` | 10 | Operasi CRUD perangkat, validasi input, status bawaan, soft delete |
| `TelemetryServiceTest` | 8 | Logika push sensor, pencarian bucket tanggal, rentang historis, paginasi |
| `DiscordWebhookServiceTest` | 6 | Posting embed notifikasi, penanganan kegagalan jaringan, URL kosong |
| `MqttIngestionEngineTest` | 7 | Validasi pesan topik, pengabaian ID tak dikenal, aturan alarm suhu overheat |
| `SensorWebSocketHandlerTest` | 6 | Manajemen sesi WebSocket, koneksi terisolasi, multikoneksi, kegagalan IOException |
| `GlobalExceptionHandlerTest` | 6 | Penanganan exception HTTP global dan format respons error |
| `DeviceControllerTest` | 8 | Panggilan REST perangkat, tanggapan 400/404, validasi UUID |
| `TelemetryControllerTest` | 8 | Ingesti via HTTP, data terbaru, rentang historis, status empty object |
| `ReadingKeyTest` | 5 | Kontrak kesamaan (`equals`), kode hash (`hashCode`), dan getter partisi |

> Unit test controller menggunakan `MockMvcBuilders.standaloneSetup()` untuk menjamin pengujian berjalan ringan tanpa memuat modul Spring Context penuh.

---

## Struktur Proyek

```
src/main/java/com/iot/deviceapi/
├── config/
│   ├── CassandraConfig.java       # Konfigurasi koneksi Cassandra
│   ├── OpenApiConfig.java         # Konfigurasi Swagger UI
│   └── WebSocketConfig.java       # Registrasi endpoint WebSocket
├── controller/
│   ├── docs/                      # Anotasi custom Swagger/OpenAPI untuk respons error
│   │   ├── ApiBadRequestError.java
│   │   ├── ApiInternalServerError.java
│   │   └── ApiNotFoundError.java
│   ├── DeviceController.java      # Implementasi endpoint REST CRUD perangkat
│   ├── DeviceControllerDocs.java  # Interface kontrak & dokumentasi Swagger perangkat
│   ├── TelemetryController.java   # Implementasi endpoint REST push & query telemetri
│   └── TelemetryControllerDocs.java # Interface kontrak & dokumentasi Swagger telemetri
├── exception/
│   ├── ApiErrorResponse.java      # Model respons error standar
│   └── GlobalExceptionHandler.java # Penanganan error terpusat
├── handler/
│   └── SensorWebSocketHandler.java # Broadcast real-time ke dashboard
├── model/
│   ├── Device.java                # Entity JPA perangkat
│   ├── DeviceInput.java           # DTO input perangkat
│   ├── Reading.java               # Entity Cassandra telemetri
│   ├── ReadingInput.java          # DTO input telemetri
│   ├── ReadingKey.java            # Primary key komposit Cassandra
│   └── WebSocketEvent.java        # Wrapper event WebSocket
├── repository/
│   ├── DeviceRepository.java      # JPA repository perangkat
│   └── ReadingRepository.java     # Cassandra repository telemetri
└── service/
    ├── DeviceService.java          # Logika bisnis perangkat
    ├── DiscordWebhookService.java  # Notifikasi Discord webhook
    ├── MqttIngestionEngine.java    # Subscriber & prosesor MQTT
    └── TelemetryService.java       # Logika bisnis telemetri

src/test/java/com/iot/deviceapi/
├── controller/                    # Test slice controller
├── exception/                     # Test exception handler
├── handler/                       # Test WebSocket handler
├── model/                         # Test model (ReadingKey)
└── service/                       # Test service layer
```

---

## Swagger / OpenAPI

Dokumentasi Swagger UI interaktif dapat diakses pada URI berikut ketika server berjalan:

```
http://localhost:3000/docs-ui
```

---

## Catatan Teknis

- **Connection Pool (HikariCP):** Dikonfigurasi secara eksternal melalui variabel environment `DB_POOL_*`. Nilai default telah disetel untuk mengoptimalkan kinerja throughput koneksi database relasional.
- **Partisi Cassandra:** Pembacaan data telemetri dipartisi berdasarkan `(device_id, bucket_date)` dan diurutkan secara menurun (`descending`) berdasarkan `ts_device`. Untuk query historis lintas hari, aplikasi melakukan beberapa query paralel per hari untuk menggabungkan hasilnya di memori.
- **Auto-Reconnect MQTT:** subscriber menggunakan pustaka Paho dengan pengaturan `automaticReconnect=true` dan selang waktu coba ulang 5 detik agar koneksi tetap persisten apabila broker terputus.
- **Thread Safety WebSocket:** Broadcast telemetri ke klien aktif menggunakan `CopyOnWriteArrayList` agar thread HTTP dan MQTT dapat menyebarkan telemetri secara asinkron tanpa menimbulkan masalah konkurensi.
