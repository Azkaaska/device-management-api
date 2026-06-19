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
        console.warn('MQTT Ingestion Engine: Connection lost. Transitioning to OFFLINE state.');
    });

    client.on('error', (err) => {
        console.error('MQTT Ingestion Engine: Broker Error event:', err.message);
    });

    client.on('message', async (topic, message) => {
        try {
            const parts = topic.split('/');
            if (parts.length !== 3) {
                console.warn(`[Parsing Drop] Invalid topic hierarchy received: ${topic}`);
                return;
            }
            
            const deviceId = parts[2];
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
            if (!uuidRegex.test(deviceId)) {
                console.warn(`[Parsing Drop] Invalid UUID string token found: ${deviceId}`);
                return;
            }

            const payload = JSON.parse(message.toString());
            const { ts, sensor_values } = payload;

            if (!ts || !sensor_values) {
                console.warn(`[Parsing Drop] Missing 'ts' or 'sensor_values' field in payload.`);
                return;
            }

            const savedResult = await telemetryService.saveTelemetry(deviceId, sensor_values, ts);
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
