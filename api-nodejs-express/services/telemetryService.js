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

    async getTelemetry(deviceId, startTime, endTime, page = 1, limit = 20) {
        const hasDevice = await this.verifyDeviceExists(deviceId);
        if (!hasDevice) return null;

        if (startTime !== undefined && endTime !== undefined) {
            return await Reading.getHistorical(deviceId, startTime, endTime, page, limit);
        }
        
        const latest = await Reading.getLatest(deviceId);
        return latest ? { isLatest: true, data: latest } : { isLatest: true, data: {} };
    }
}

module.exports = new TelemetryService();
