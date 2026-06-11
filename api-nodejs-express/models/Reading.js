const { DataTypes } = require('sequelize');
const sequelize = require('../database');

const Reading = sequelize.define('Reading', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    device_id: {
        type: DataTypes.UUID,
        allowNull: false
    },
    sensor_values: {
        type: DataTypes.JSONB,
        allowNull: false
    },
    ts: {
        type: DataTypes.BIGINT,
        allowNull: false
    }
}, {
    tableName: 'readings',
    timestamps: false,
    hooks: {
        beforeValidate: (reading, options) => {
            if (!reading.ts) {
                reading.ts = Date.now();
            }
        }
    }
});

module.exports = Reading;
