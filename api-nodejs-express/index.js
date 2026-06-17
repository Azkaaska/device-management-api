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
            // expect: place1 / place2 / deviceId
            if (parts.length !== 3) return;
            const deviceId = parts[2];
            const payload = JSON.parse(message.toString());
            const ts = payload.ts;
            const sensorValues = payload.sensor_values || {};

            const device = await Device.findByPk(deviceId);
            if (!device) {
                console.log(`MQTT Ingestion: Unknown device ${deviceId}, skipping`);
                return;
            }

            await Reading.save(deviceId, sensorValues, ts);
            console.log(`MQTT Ingestion: Saved reading for device ${deviceId}`);
        } catch (err) {
            console.error('MQTT Ingestion processing error:', err.message);
        }
    });

    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Failed to sync database:', err);
});
