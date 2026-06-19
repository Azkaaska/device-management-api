const mqtt = require('mqtt');
const telemetryService = require('../services/telemetryService');

function bootstrapMqttWorker() {
    const mqttHost = process.env.MQTT_HOST || '127.0.0.1';
    const mqttPort = process.env.MQTT_PORT || '1883';

    const mqttOptions = {
        reconnectPeriod: 5000,
        connectTimeout: 30000,
    };

    console.log(`MQTT Ingestion Engine: Connecting to broker at mqtt://${mqttHost}:${mqttPort}...`);
    const client = mqtt.connect(`mqtt://${mqttHost}:${mqttPort}`, mqttOptions);

    client.on('connect', () => {
        console.log(`MQTT Ingestion Engine: Connected successfully.`);
        client.subscribe('+/+/+');
    });

    client.on('offline', () => {
        console.warn('MQTT Ingestion Engine: Connection lost.');
    });

    client.on('error', (err) => {
        console.error('MQTT Ingestion Engine Error:', err.message);
    });

    client.on('message', async (topic, message) => {
        try {
            const parts = topic.split('/');
            if (parts.length !== 3) return;
            
            const deviceId = parts[2];
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
            if (!uuidRegex.test(deviceId)) return;

            const payload = JSON.parse(message.toString());
            const { ts, temperature, humidity } = payload;

            if (temperature === undefined || humidity === undefined) {
                console.warn(`[Parsing Drop] Missing numeric sensor metrics on topic: ${topic}`);
                return;
            }

            const tsDevice = ts || Date.now();
            const savedResult = await telemetryService.saveTelemetry(deviceId, temperature, humidity, tsDevice);
            
            if (!savedResult) {
                console.log(`MQTT Ingestion Engine: Unknown device parsed from ID (${deviceId}), telemetry skipped.`);
                return;
            }

            console.log(`MQTT Ingestion Engine: Saved device data pipeline entry for ${deviceId}`);
        } catch (err) {
            console.error('[MQTT Ingestion Engine Error]: Stream processing fault:', err.message);
        }
    });
}

module.exports = { bootstrapMqttWorker };
