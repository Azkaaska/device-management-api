const express = require('express');
const router = express.Router();
const { Device } = require('../models');

router.post('/', async (req, res) => {
    try {
        const { name, type, status } = req.body;
        if (!name) {
            return res.status(400).json({ error: 'Name is required' });
        }
        const device = await Device.create({ name, type, status });
        res.status(201).json(device);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.get('/', async (req, res) => {
    try {
        const devices = await Device.findAll();
        res.json(devices);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.get('/:id', async (req, res) => {
    try {
        const device = await Device.findByPk(req.params.id);
        if (device) {
            res.json(device);
        } else {
            res.status(404).json({ error: 'Device not found' });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.put('/:id', async (req, res) => {
    try {
        const { name, type, status } = req.body;
        if (!name) {
            return res.status(400).json({ error: 'Name is required' });
        }
        
        const device = await Device.findByPk(req.params.id);
        if (!device) {
            return res.status(404).json({ error: 'Device not found' });
        }

        device.name = name;
        if (type !== undefined) device.type = type;
        if (status !== undefined) device.status = status;
        
        await device.save();
        res.json(device);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.delete('/:id', async (req, res) => {
    try {
        const device = await Device.findByPk(req.params.id);
        if (device) {
            await device.destroy();
            res.status(204).end();
        } else {
            res.status(404).json({ error: 'Device not found' });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;
