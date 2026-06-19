const { Reading, Device } = require('../models');

class TelemetryService {
    async verifyDeviceExists(deviceId) {
        const count = await Device.count({ where: { device_id: deviceId } });
        return count > 0;
    }

    async saveTelemetry(deviceId, sensorValues, timestamp = Date.now()) {
        const hasDevice = await this.verifyDeviceExists(deviceId);
        if (!hasDevice) return null;

        return await Reading.save(deviceId, sensorValues, timestamp);
    }

    async getTelemetry(deviceId, startTime, endTime) {
        const hasDevice = await this.verifyDeviceExists(deviceId);
        if (!hasDevice) return null;

        if (startTime !== undefined && endTime !== undefined) {
            return await Reading.getHistorical(deviceId, parseInt(startTime, 10), parseInt(endTime, 10));
        }
        
        const latest = await Reading.getLatest(deviceId);
        return latest || {};
    }
}

module.exports = new TelemetryService();
