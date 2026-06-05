const sequelize = require('../database');
const Device = require('./Device');
const Telemetry = require('./Telemetry');

// Define Associations
Device.hasMany(Telemetry, { foreignKey: 'deviceId', onDelete: 'CASCADE' });
Telemetry.belongsTo(Device, { foreignKey: 'deviceId' });

module.exports = {
    sequelize,
    Device,
    Telemetry
};
