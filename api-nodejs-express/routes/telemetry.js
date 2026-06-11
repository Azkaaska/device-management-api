const express = require('express');
const router = express.Router({ mergeParams: true });
const { Device, Reading } = require('../models');
const { Op } = require('sequelize');

router.get('/:id/telemetry', async (req, res) => {
    try {
        const { id } = req.params;
        const { start_time, end_time } = req.query;
        
        const deviceExists = await Device.count({ where: { device_id: id } });
        if (!deviceExists) {
            return res.status(404).json({ error: 'Device not found' });
        }

        if (start_time !== undefined && end_time !== undefined) {
            const readings = await Reading.findAll({
                where: {
                    device_id: id,
                    ts: {
                        [Op.gte]: parseInt(start_time, 10),
                        [Op.lte]: parseInt(end_time, 10)
                    }
                },
                order: [['ts', 'DESC']]
            });
            res.json(readings);
        } else {
            const telemetry = await Reading.findOne({
                where: { device_id: id },
                order: [['ts', 'DESC']]
            });
            res.json(telemetry || {});
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
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
        
        const telemetry = await Reading.create({
            device_id: id,
            sensor_values,
            ts: Date.now()
        });
        
        res.status(201).json(telemetry);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;
