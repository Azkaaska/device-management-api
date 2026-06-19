const { DataTypes } = require('sequelize');
const sequelize = require('../config/postgres');

const Device = sequelize.define('Device', {
    device_id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true,
        field: 'device_id'
    },
    device_name: {
        type: DataTypes.STRING(100),
        allowNull: false,
        field: 'device_name'
    },
    device_type: {
        type: DataTypes.STRING(50),
        allowNull: false,
        field: 'device_type'
    },
    status: {
        type: DataTypes.STRING(20),
        allowNull: false,
        defaultValue: 'ACTIVE',
        field: 'status'
    },
    firmware_version: {
        type: DataTypes.STRING(20),
        allowNull: true,
        field: 'firmware_version'
    },
    device_metadata: {
        type: DataTypes.JSONB,
        allowNull: true,
        field: 'device_metadata'
    },
    created_at: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'created_at'
    },
    updated_at: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'updated_at'
    }
}, {
    tableName: 'devices',
    timestamps: false,
    indexes: [
        { name: 'idx_devices_type', fields: ['device_type'] },
        { name: 'idx_devices_status_type', fields: ['status', 'device_type'] }
    ],
    hooks: {
        beforeValidate: (device) => {
            const now = Date.now();
            if (!device.created_at) {
                device.created_at = now;
            }
            device.updated_at = now;
        },
        beforeUpdate: (device) => {
            device.updated_at = Date.now();
        }
    }
});

module.exports = Device;
