const { DataTypes } = require('sequelize');
const sequelize = require('../database');

const Device = sequelize.define('Device', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    name: {
        type: DataTypes.STRING,
        allowNull: false
    },
    type: {
        type: DataTypes.STRING,
        allowNull: true
    },
    status: {
        type: DataTypes.STRING,
        defaultValue: 'ACTIVE'
    },
    createdAt: {
        type: DataTypes.BIGINT,
        allowNull: false
    }
}, {
    tableName: 'devices',
    timestamps: false, 
    hooks: {
        beforeValidate: (device, options) => {
            if (!device.createdAt) {
                device.createdAt = Date.now();
            }
        }
    }
});

module.exports = Device;
