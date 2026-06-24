# Device Management API — Java / Spring Boot

Implementasi IoT Device Management API menggunakan **Java 25** dan **Spring Boot 4**. Lihat [README utama](../README.md) untuk dokumentasi endpoint dan arsitektur yang dibagikan antar ketiga implementasi.

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

Pastikan layanan berikut sudah berjalan sebelum menjalankan aplikasi:

- **JDK 25+**
- **PostgreSQL** — database untuk metadata perangkat
- **Apache Cassandra** — database untuk data telemetri time-series
- **MQTT Broker** (mis. Eclipse Mosquitto) — untuk ingesti data sensor

---

## Konfigurasi Environment

Salin file `.env.example` menjadi `.env` dan isi dengan nilai yang sesuai:

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

## Cara Menjalankan

```bash
# Jalankan dalam mode development
.\mvnw.cmd spring-boot:run

# Atau build dulu lalu jalankan JAR-nya
.\mvnw.cmd package -DskipTests
java -jar target/deviceapi-0.0.1-SNAPSHOT.jar

# Gunakan ./mvnw pada shell berbasis unix/linux
```

Server akan berjalan di `http://localhost:3000`.

---

## Menjalankan Unit Test

```bash
.\mvnw.cmd test

# Gunakan ./mvnw pada shell berbasis unix/linux
```

Total **66 unit test** yang mencakup semua lapisan aplikasi:

| Kelas Test | Jumlah Test | Cakupan |
|---|---|---|
| `DeviceServiceTest` | 10 | CRUD, validasi input, soft delete, default status |
| `TelemetryServiceTest` | 8 | Push data, pemindaian bucket, query historis, paginasi |
| `DiscordWebhookServiceTest` | 6 | Posting webhook, URL kosong, kegagalan jaringan |
| `MqttIngestionEngineTest` | 7 | Paket valid, perangkat tidak dikenal, topik salah, overheat |
| `SensorWebSocketHandlerTest` | 6 | Lifecycle sesi, multi-broadcast, sesi tertutup, isolasi IOException |
| `GlobalExceptionHandlerTest` | 6 | Semua handler exception + timestamp `ApiErrorResponse` |
| `DeviceControllerTest` | 8 | Semua endpoint REST, format error 400/404, UUID salah |
| `TelemetryControllerTest` | 8 | Latest/historis, push, response kosong, JSON rusak |
| `ReadingKeyTest` | 5 | Kontrak `equals`/`hashCode`, getter |

> Unit test controller menggunakan `MockMvcBuilders.standaloneSetup()` (tanpa Spring context penuh) agar lebih ringan dan cepat.

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

Dokumentasi interaktif tersedia setelah server berjalan di:

```
http://localhost:3000/docs-ui
```

---

## Catatan Teknis

- **Connection Pool (HikariCP):** Dikonfigurasi melalui variabel environment `DB_POOL_*`. Default sudah dioptimalkan untuk workload IoT dengan koneksi yang tahan lama.
- **Partisi Cassandra:** Data telemetri dipartisi berdasarkan `(device_id, bucket_date)` dan dicluster secara descending berdasarkan `ts_device`. Query historis memerlukan iterasi per hari karena batasan partisi Cassandra.
- **MQTT Reconnect:** `MqttIngestionEngine` menggunakan `automaticReconnect=true` dengan loop retry 5 detik sebagai fallback. Engine berjalan sebagai daemon thread agar tidak memblokir shutdown aplikasi.
- **WebSocket Thread Safety:** `SensorWebSocketHandler` menggunakan `CopyOnWriteArrayList` sehingga thread MQTT dan HTTP dapat memanggil `broadcast()` secara bersamaan tanpa race condition.
- **Best-effort Broadcast:** Kegagalan serialisasi atau pengiriman WebSocket tidak akan membatalkan proses penyimpanan data telemetri.
