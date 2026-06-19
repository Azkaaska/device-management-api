const { DataTypes } = require('sequelize');
const sequelize = require('../config/postgres');

const Device = sequelize.define('Device', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    name: {
        type: DataTypes.STRING(100),
        allowNull: false
    },
    type: {
        type: DataTypes.STRING(50),
        allowNull: false
    },
    status: {
        type: DataTypes.STRING(20),
        allowNull: false,
        defaultValue: 'active'
    }
}, {
    tableName: 'devices',
    timestamps: false
});

module.exports = Device;
