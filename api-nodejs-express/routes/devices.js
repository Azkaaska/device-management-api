const express = require('express');
const router = express.Router();
const { Device } = require('../models');
const sequelize = require('../database/postgres');
const { ValidationError, UniqueConstraintError } = require('sequelize');

router.get('/', async (req, res) => {
    try {
        const devices = await Device.findAll();
        res.json(devices);
    } catch (err) {
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

router.post('/', async (req, res) => {
    const t = await sequelize.transaction();
    try {
        const { device_name, device_type, status, firmware_version, device_metadata } = req.body;
        if (!device_name) {
            await t.rollback();
            return res.status(400).json({ error: 'device_name is required' });
        }
        const device = await Device.create(
            { device_name, device_type, status, firmware_version, device_metadata },
            { transaction: t }
        );
        await t.commit();
        res.status(201).json(device);
    } catch (err) {
        await t.rollback();
        if (err instanceof UniqueConstraintError) {
            return res.status(409).json({ error: 'Conflict', detail: err.message });
        }
        if (err instanceof ValidationError) {
            return res.status(400).json({ error: 'Validation error', detail: err.message });
        }
        res.status(500).json({ error: 'Internal server error', detail: err.message });
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
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

router.put('/:id', async (req, res) => {
    const t = await sequelize.transaction();
    try {
        const { device_name, device_type, status, firmware_version, device_metadata } = req.body;
        if (!device_name) {
            await t.rollback();
            return res.status(400).json({ error: 'device_name is required' });
        }

        const device = await Device.findByPk(req.params.id, { transaction: t });
        if (!device) {
            await t.rollback();
            return res.status(404).json({ error: 'Device not found' });
        }

        device.device_name = device_name;
        device.device_type = device_type;
        if (status !== undefined) device.status = status;
        device.firmware_version = firmware_version;
        device.device_metadata = device_metadata;

        await device.save({ transaction: t });
        await t.commit();
        res.json(device);
    } catch (err) {
        await t.rollback();
        if (err instanceof UniqueConstraintError) {
            return res.status(409).json({ error: 'Conflict', detail: err.message });
        }
        if (err instanceof ValidationError) {
            return res.status(400).json({ error: 'Validation error', detail: err.message });
        }
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

router.delete('/:id', async (req, res) => {
    const t = await sequelize.transaction();
    try {
        const device = await Device.findByPk(req.params.id, { transaction: t });
        if (!device) {
            await t.rollback();
            return res.status(404).json({ error: 'Device not found' });
        }
        device.status = 'INACTIVE';
        await device.save({ transaction: t });
        await t.commit();
        res.status(204).end();
    } catch (err) {
        await t.rollback();
        res.status(500).json({ error: 'Internal server error', detail: err.message });
    }
});

module.exports = router;
