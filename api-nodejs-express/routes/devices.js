const express = require('express');
const router = express.Router();
const { Device } = require('../models');

router.get('/', async (req, res) => {
    try {
        const devices = await Device.findAll();
        res.json(devices);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.post('/', async (req, res) => {
    try {
        const { device_name, device_type, status, firmware_version, device_metadata } = req.body;
        if (!device_name) {
            return res.status(400).json({ error: 'device_name is required' });
        }
        const device = await Device.create({
            device_name,
            device_type,
            status,
            firmware_version,
            device_metadata
        });
        res.status(201).json(device);
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
        const { device_name, device_type, status, firmware_version, device_metadata } = req.body;
        if (!device_name) {
            return res.status(400).json({ error: 'device_name is required' });
        }
        
        const device = await Device.findByPk(req.params.id);
        if (!device) {
            return res.status(404).json({ error: 'Device not found' });
        }
        
        device.device_name = device_name;
        device.device_type = device_type;
        if (status !== undefined) device.status = status;
        device.firmware_version = firmware_version;
        device.device_metadata = device_metadata;
        
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
            device.status = 'INACTIVE';
            await device.save();
            res.status(204).end();
        } else {
            res.status(404).json({ error: 'Device not found' });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;
