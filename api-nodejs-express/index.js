const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const { sequelize } = require('./models');
const devicesRouter = require('./routes/devices');
const telemetryRouter = require('./routes/telemetry');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.disable('x-powered-by');

const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/docs-ui', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use('/api/devices', devicesRouter);
app.use('/api/devices', telemetryRouter);

sequelize.sync().then(() => {
    console.log('Database synced');
    
    // Start MQTT subscriber
    const mqtt = require('mqtt');
    const { Device, Reading } = require('./models');
    const mqttHost = process.env.MQTT_HOST || '127.0.0.1';
    const mqttPort = process.env.MQTT_PORT || '1883';
    const client = mqtt.connect(`mqtt://${mqttHost}:${mqttPort}`);

    client.on('connect', () => {
        console.log(`MQTT Ingestion: Connected to broker (mqtt://${mqttHost}:${mqttPort})`);
        // topic: {place1}/{place2}/{deviceId}
        client.subscribe('+/+/+');
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
            const ts = payload.ts;
            const sensorValues = payload.sensor_values;

            if (!ts || !sensorValues) {
                console.warn(`[Parsing Drop] Missing 'ts' or 'sensor_values' field in payload.`);
                return;
            }

            const device = await Device.findByPk(deviceId);
            if (!device) {
                console.log(`MQTT Ingestion: Unknown device registered inside database ${deviceId}, skipping save`);
                return;
            }

            await Reading.save(deviceId, sensorValues, ts);
            console.log(`MQTT Ingestion: Successfully saved telemetry for device ${deviceId}`);

        } catch (err) {
            console.error('[MQTT Ingestion Engine] Stream processing error:', err.message);
        }
    });

    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Failed to sync database:', err);
});
