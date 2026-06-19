const mqtt = require('mqtt');

const mqttHost = process.env.MQTT_HOST || '127.0.0.1';
const mqttPort = process.env.MQTT_PORT || '1883';

const devices = [
    {
        deviceId: '550e8400-e29b-41d4-a716-446655440001',
        deviceType: 'thermometer',
        topic: 'buildingA/floor2/550e8400-e29b-41d4-a716-446655440001'
    },
    {
        deviceId: '550e8400-e29b-41d4-a716-446655440002',
        deviceType: 'energymeter',
        topic: 'plantB/chiller4/550e8400-e29b-41d4-a716-446655440002'
    },
    {
        deviceId: '550e8400-e29b-41d4-a716-446655440003',
        deviceType: 'waterflow',
        topic: 'plantB/coolingTower/550e8400-e29b-41d4-a716-446655440003'
    }
];

let isConnected = false;
const offlineBuffer = [];

const mqttOptions = {
    reconnectPeriod: 5000,
    connectTimeout: 30000
};

console.log(`Starting Emulator Stream... Connecting to mqtt://${mqttHost}:${mqttPort}`);
const client = mqtt.connect(`mqtt://${mqttHost}:${mqttPort}`, mqttOptions);

client.on('connect', () => {
    console.log(`Emulator status: CONNECTED to MQTT Broker (mqtt://${mqttHost}:${mqttPort})`);
    isConnected = true;

    if (offlineBuffer.length > 0) {
        console.log(`[Reconnection Logic] Recovered connection. Flushing ${offlineBuffer.length} pending items out of queue...`);
        
        while (offlineBuffer.length > 0) {
            const bufferedData = offlineBuffer.shift();
            client.publish(bufferedData.topic, bufferedData.message, { qos: 1 });
        }
        console.log('[Reconnection Logic] Buffer queue cleared successfully!');
    }
});

client.on('offline', () => {
    if (isConnected) {
        console.warn('Emulator status: DISCONNECTED from Broker. Switching telemetry pipelines to Buffering state.');
        isConnected = false;
    }
});

client.on('error', (err) => {
    console.error('Emulator Client Connection Exception:', err.message);
});

let energyAccumulated = Math.random() * 40.0;
let waterVolumeAccumulated = Math.random() * 400.0;

setInterval(() => {
    const ts = Date.now();

    for (const device of devices) {
        let sensorValues = {};

        if (device.deviceType === 'thermometer') {
            sensorValues = {
                temperature: parseFloat((Math.random() * 15 + 20).toFixed(2)),
                humidity: parseFloat((Math.random() * 40 + 40).toFixed(2))
            };
        } else if (device.deviceType === 'energymeter') {
            const voltage = parseFloat((Math.random() * 20 + 220).toFixed(1));
            const current = parseFloat((Math.random() * 7.5 + 0.5).toFixed(2));
            const active_power = parseFloat(((voltage * current * 0.9) / 1000).toFixed(3));
            energyAccumulated += active_power * (2 / 3600);
            sensorValues = { voltage, current, active_power, total_energy: parseFloat(energyAccumulated.toFixed(4)) };
        } else if (device.deviceType === 'waterflow') {
            const flow_rate = parseFloat((Math.random() * 20 + 5).toFixed(2));
            waterVolumeAccumulated += flow_rate * (2 / 60);
            sensorValues = { flow_rate, total_volume: parseFloat(waterVolumeAccumulated.toFixed(2)) };
        }

        const payload = { ts, sensor_values: sensorValues };

        let serializedPayload;
        try {
            serializedPayload = JSON.stringify(payload);
        } catch (err) {
            console.error(`Serialization Error for device ${device.deviceId}:`, err.message);
            continue;
        }

        if (isConnected) {
            client.publish(device.topic, serializedPayload, { qos: 1 });
            console.log(`[ONLINE] [${device.deviceType}] → ${device.topic} | ${serializedPayload}`);
        } else {
            console.log(`[OFFLINE BUFFERING] Preserving packet for: ${device.topic}`);
            offlineBuffer.push({ topic: device.topic, message: serializedPayload });
            
            if (offlineBuffer.length > 5000) {
                offlineBuffer.shift();
            }
        }
    }
}, 2000);
