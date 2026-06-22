const { Reading, Device } = require('../models');
const eventHub = require('../utils/eventHub');

class TelemetryService {
    async verifyDeviceExists(deviceId) {
        const count = await Device.count({ where: { id: deviceId } });
        return count > 0;
    }

    async saveTelemetry(deviceId, temperature, humidity, tsDevice) {
        const hasDevice = await this.verifyDeviceExists(deviceId);
        if (!hasDevice) return null;

        const telemetry = await Reading.save(deviceId, temperature, humidity, tsDevice);
        
        if (telemetry) {
            // Publish telemetry to the event hub for real-time consumers
            eventHub.emit('telemetry:new', telemetry);
        }

        return telemetry;
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
