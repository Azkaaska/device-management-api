const { Device } = require('../models');
const sequelize = require('../config/postgres');

class DeviceService {
    async getAllDevices() {
        return await Device.findAll();
    }

    async getDeviceById(id) {
        return await Device.findByPk(id);
    }

    async createDevice(data) {
        const t = await sequelize.transaction();
        try {
            const device = await Device.create(data, { transaction: t });
            await t.commit();
            return device;
        } catch (err) {
            await t.rollback();
            throw err;
        }
    }

    async updateDevice(id, data) {
        const t = await sequelize.transaction();
        try {
            const device = await Device.findByPk(id, { transaction: t });
            if (!device) return null;

            Object.assign(device, data);
            await device.save({ transaction: t });
            await t.commit();
            return device;
        } catch (err) {
            await t.rollback();
            throw err;
        }
    }

    async softDeleteDevice(id) {
        const t = await sequelize.transaction();
        try {
            const device = await Device.findByPk(id, { transaction: t });
            if (!device) return false;

            device.status = 'INACTIVE';
            await device.save({ transaction: t });
            await t.commit();
            return true;
        } catch (err) {
            await t.rollback();
            throw err;
        }
    }
}

module.exports = new DeviceService();
