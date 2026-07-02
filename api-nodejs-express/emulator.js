const mqtt = require('mqtt');

const mqttHost = process.env.MQTT_HOST || '127.0.0.1';
const mqttPort = process.env.MQTT_PORT || '1883';

const devices = [
    {
        id: '4cdf2bf1-f43d-4308-b64e-9fcaa0d50236',
        topic: 'buildingA/floor1/4cdf2bf1-f43d-4308-b64e-9fcaa0d50236'
    }
];

console.log(`Starting Emulator Stream... Connecting to mqtt://${mqttHost}:${mqttPort}`);
const client = mqtt.connect(`mqtt://${mqttHost}:${mqttPort}`, {
    reconnectPeriod: 5000,
    connectTimeout: 30000
});

client.on('connect', () => {
    console.log(`Emulator status: CONNECTED to MQTT Broker.`);
});

setInterval(() => {
    for (const device of devices) {
        const payload = {
            ts: Date.now(),
            temperature: parseFloat((Math.random() * 15 + 20).toFixed(1)),
            humidity: parseFloat((Math.random() * 40 + 40).toFixed(1))
        };

        const serializedPayload = JSON.stringify(payload);
        client.publish(device.topic, serializedPayload, { qos: 1 });
        console.log(`[ONLINE EMULATOR] → ${device.topic} | ${serializedPayload}`);
    }
}, 5000);
