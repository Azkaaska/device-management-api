const express = require('express');
const router = express.Router({ mergeParams: true });
const { Device, Telemetry } = require('../models');
const { ForeignKeyConstraintError } = require('sequelize');

router.post('/:id/telemetry', async (req, res) => {
    try {
        const { id } = req.params;
        const { temperature, humidity } = req.body;
        
        const telemetry = await Telemetry.create({
            deviceId: id,
            temperature,
            humidity,
            ts: Date.now()
        });
        
        res.status(201).json(telemetry);
    } catch (err) {
        if (err instanceof ForeignKeyConstraintError) {
            return res.status(404).json({ error: 'Device not found' });
        }
        res.status(500).json({ error: err.message });
    }
});

router.get('/:id/telemetry', async (req, res) => {
    try {
        const { id } = req.params;
        
        const deviceExists = await Device.count({ where: { id: id } });
        if (!deviceExists) {
            return res.status(404).json({ error: 'Device not found' });
        }

        const telemetry = await Telemetry.findOne({
            where: { deviceId: id },
            order: [['ts', 'DESC']]
        });
        
        res.json(telemetry || {});
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;
