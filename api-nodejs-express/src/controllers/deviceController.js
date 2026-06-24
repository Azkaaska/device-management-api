const deviceService = require('../services/deviceService');

class DeviceController {
    async getAll(req, res, next) {
        try {
            const page = parseInt(req.query.page, 10) || 1;
            const limit = parseInt(req.query.limit, 10) || 10;

            const result = await deviceService.getAllDevices(page, limit);
            
            res.json({
                data: result.devices,
                pagination: {
                    total_items: result.totalItems,
                    total_pages: result.totalPages,
                    current_page: result.currentPage,
                    limit: limit
                }
            });
        } catch (err) {
            next(err);
        }
    }

    async getById(req, res, next) {
        try {
            const device = await deviceService.getDeviceById(req.params.id);
            if (!device) return res.status(404).json({ error: 'Device not found' });
            res.json(device);
        } catch (err) {
            next(err);
        }
    }

    async create(req, res, next) {
        try {
            const device = await deviceService.createDevice(req.body);
            res.status(201).json(device);
        } catch (err) {
            next(err);
        }
    }

    async update(req, res, next) {
        try {
            const device = await deviceService.updateDevice(req.params.id, req.body);
            if (!device) return res.status(404).json({ error: 'Device not found' });
            res.json(device);
        } catch (err) {
            next(err);
        }
    }

    async delete(req, res, next) {
        try {
            const success = await deviceService.softDeleteDevice(req.params.id);
            if (!success) return res.status(404).json({ error: 'Device not found' });
            res.status(204).end();
        } catch (err) {
            next(err);
        }
    }
}

module.exports = new DeviceController();
