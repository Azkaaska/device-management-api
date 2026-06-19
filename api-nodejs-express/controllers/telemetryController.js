const telemetryService = require('../services/telemetryService');

class TelemetryController {
    async getTelemetry(req, res, next) {
        try {
            const { id } = req.params;
            const { start_time, end_time } = req.query;
            
            const page = parseInt(req.query.page, 10) || 1;
            const limit = parseInt(req.query.limit, 10) || 20;

            const result = await telemetryService.getTelemetry(id, start_time, end_time, page, limit);
            if (result === null) return res.status(404).json({ error: 'Device not found' });
            
            if (!result.isLatest) {
                return res.json({
                    data: result.readings,
                    pagination: {
                        total_items: result.totalItems,
                        total_pages: result.totalPages,
                        current_page: result.currentPage,
                        limit: limit
                    }
                });
            }

            res.json(result.data);
        } catch (err) {
            next(err);
        }
    }

    async postTelemetry(req, res, next) {
        try {
            const { id } = req.params;
            const { ts, temperature, humidity } = req.body;

            if (temperature === undefined || humidity === undefined) {
                return res.status(400).json({ error: 'temperature and humidity parameters are required' });
            }

            const tsDevice = ts || Date.now();
            const telemetry = await telemetryService.saveTelemetry(id, temperature, humidity, tsDevice);
            
            if (!telemetry) return res.status(404).json({ error: 'Device not found' });

            res.status(201).json(telemetry);
        } catch (err) {
            next(err);
        }
    }
}

module.exports = new TelemetryController();
