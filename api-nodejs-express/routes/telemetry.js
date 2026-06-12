const express = require('express');
const router = express.Router({ mergeParams: true });
const { Device, Reading } = require('../models');

router.get('/:id/telemetry', async (req, res) => {
    try {
        const { id } = req.params;
        const { start_time, end_time } = req.query;

        const deviceExists = await Device.count({ where: { device_id: id } });
        if (!deviceExists) {
            return res.status(404).json({ error: 'Device not found' });
        }

        if (start_time !== undefined && end_time !== undefined) {
            const readings = await Reading.getHistorical(id, parseInt(start_time, 10), parseInt(end_time, 10));
            res.json(readings);
        } else {
            const telemetry = await Reading.getLatest(id);
            res.json(telemetry || {});
        }
    } catch (err) {
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

router.post('/:id/telemetry', async (req, res) => {
    try {
        const { id } = req.params;
        const { sensor_values } = req.body;

        const deviceExists = await Device.count({ where: { device_id: id } });
        if (!deviceExists) {
            return res.status(404).json({ error: 'Device not found' });
        }

        const telemetry = await Reading.save(id, sensor_values, Date.now());
        res.status(201).json(telemetry);
    } catch (err) {
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

module.exports = router;
