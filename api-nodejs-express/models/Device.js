const { DataTypes } = require('sequelize');
const sequelize = require('../database');

const Device = sequelize.define('Device', {
    device_id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    device_name: {
        type: DataTypes.STRING(100),
        allowNull: false
    },
    device_type: {
        type: DataTypes.STRING(50),
        allowNull: false
    },
    status: {
        type: DataTypes.STRING(20),
        allowNull: false,
        defaultValue: 'ACTIVE'
    },
    firmware_version: {
        type: DataTypes.STRING(20),
        allowNull: true
    },
    device_metadata: {
        type: DataTypes.JSONB,
        allowNull: true
    },
    created_at: {
        type: DataTypes.BIGINT,
        allowNull: false
    },
    updated_at: {
        type: DataTypes.BIGINT,
        allowNull: false
    }
}, {
    tableName: 'devices',
    timestamps: false, 
    hooks: {
        beforeValidate: (device, options) => {
            const now = Date.now();
            if (!device.created_at) {
                device.created_at = now;
            }
            device.updated_at = now;
        },
        beforeUpdate: (device, options) => {
            device.updated_at = Date.now();
        }
    }
});

module.exports = Device;
