const telemetryService = require('../services/telemetryService');

class TelemetryController {
    async getTelemetry(req, res, next) {
        try {
            const { id } = req.params;
            const { start_time, end_time } = req.query;

            const result = await telemetryService.getTelemetry(id, start_time, end_time);
            if (result === null) return res.status(404).json({ error: 'Device not found' });
            
            res.json(result);
        } catch (err) {
            next(err);
        }
    }

    async postTelemetry(req, res, next) {
        try {
            const { id } = req.params;
            const { sensor_values } = req.body;

            const telemetry = await telemetryService.saveTelemetry(id, sensor_values);
            if (!telemetry) return res.status(404).json({ error: 'Device not found' });

            res.status(201).json(telemetry);
        } catch (err) {
            next(err);
        }
    }
}

module.exports = new TelemetryController();