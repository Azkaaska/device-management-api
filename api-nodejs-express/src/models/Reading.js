const cassandra = require('../config/cassandra');

function getBucketDate(ts) {
    const localDate = new Date(Number(ts));
    const yyyy = localDate.getUTCFullYear();
    const mm = String(localDate.getUTCMonth() + 1).padStart(2, '0');
    const dd = String(localDate.getUTCDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

class Reading {
    static async save(deviceId, temperature, humidity, tsDevice) {
        const client = await cassandra.getSession();
        const tsReceive = Date.now();
        const bucketDate = getBucketDate(tsReceive);

        const query = `
            INSERT INTO readings (device_id, bucket_date, ts_device, ts_receive, temperature, humidity) 
            VALUES (?, ?, ?, ?, ?, ?)
        `;

        await client.execute(
            query, 
            [deviceId, bucketDate, Number(tsDevice), tsReceive, parseFloat(temperature), parseFloat(humidity)], 
            { prepare: true }
        );

        return {
            device_id: deviceId,
            bucket_date: bucketDate,
            ts_device: Number(tsDevice),
            ts_receive: tsReceive,
            temperature: parseFloat(temperature),
            humidity: parseFloat(humidity)
        };
    }

    static async getLatest(deviceId) {
        const client = await cassandra.getSession();
        const now = Date.now();
        
        for (let i = 0; i < 8; i++) {
            const checkTime = now - i * 24 * 60 * 60 * 1000;
            const bucketDate = getBucketDate(checkTime);

            const query = `
                SELECT device_id, bucket_date, ts_device, ts_receive, temperature, humidity 
                FROM readings WHERE device_id = ? AND bucket_date = ? LIMIT 1
            `;
            const result = await client.execute(query, [deviceId, bucketDate], { prepare: true });
            
            if (result.rows.length > 0) {
                const row = result.rows[0];
                return {
                    device_id: row.device_id,
                    bucket_date: row.bucket_date,
                    ts_device: Number(row.ts_device),
                    ts_receive: Number(row.ts_receive),
                    temperature: row.temperature,
                    humidity: row.humidity
                };
            }
        }
        return null;
    }

    static async getHistorical(deviceId, startTime, endTime, page = 1, limit = 20) {
        const client = await cassandra.getSession();
        const startLocal = new Date(Number(startTime) + 7 * 60 * 60 * 1000);
        const endLocal = new Date(Number(endTime) + 7 * 60 * 60 * 1000);

        const bucketDates = [];
        let current = new Date(startLocal);
        current.setUTCHours(0, 0, 0, 0);
        const endCompare = new Date(endLocal);
        endCompare.setUTCHours(0, 0, 0, 0);

        while (current <= endCompare) {
            const yyyy = current.getUTCFullYear();
            const mm = String(current.getUTCMonth() + 1).padStart(2, '0');
            const dd = String(current.getUTCDate()).padStart(2, '0');
            bucketDates.push(`${yyyy}-${mm}-${dd}`);
            current.setUTCDate(current.getUTCDate() + 1);
        }

        const allReadings = [];
        for (const bucket of bucketDates) {
            const query = `
                SELECT device_id, bucket_date, ts_device, ts_receive, temperature, humidity 
                FROM readings 
                WHERE device_id = ? AND bucket_date = ? AND ts_device >= ? AND ts_device <= ?
            `;
            const result = await client.execute(query, [deviceId, bucket, startTime, endTime], { prepare: true });
            for (const row of result.rows) {
                allReadings.push({
                    device_id: row.device_id,
                    bucket_date: row.bucket_date,
                    ts_device: Number(row.ts_device),
                    ts_receive: Number(row.ts_receive),
                    temperature: row.temperature,
                    humidity: row.humidity
                });
            }
        }

        allReadings.sort((a, b) => b.ts_device - a.ts_device);

        const totalItems = allReadings.length;
        const offset = (page - 1) * limit;
        const paginatedReadings = allReadings.slice(offset, offset + limit);

        return {
            readings: paginatedReadings,
            totalItems,
            totalPages: Math.ceil(totalItems / limit),
            currentPage: parseInt(page, 10)
        };
    }
}

module.exports = Reading;
