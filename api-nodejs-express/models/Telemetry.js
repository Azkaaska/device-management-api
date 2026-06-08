const { DataTypes } = require('sequelize');
const sequelize = require('../database');

const Telemetry = sequelize.define('Telemetry', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    device_id: {
        type: DataTypes.UUID,
        allowNull: false
    },
    temperature: {
        type: DataTypes.FLOAT
    },
    humidity: {
        type: DataTypes.FLOAT
    },
    ts: {
        type: DataTypes.BIGINT,
        allowNull: false
    }
}, {
    tableName: 'telemetry',
    timestamps: false,
    hooks: {
        beforeValidate: (telemetry, options) => {
            if (!telemetry.ts) {
                telemetry.ts = Date.now();
            }
        }
    }
});

module.exports = Telemetry;
