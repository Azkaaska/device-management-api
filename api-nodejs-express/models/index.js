const sequelize = require('../database');
const Device = require('./Device');
const Telemetry = require('./Telemetry');

// Define Associations
Device.hasMany(Telemetry, { foreignKey: 'device_id', onDelete: 'CASCADE' });
Telemetry.belongsTo(Device, { foreignKey: 'device_id' });

module.exports = {
    sequelize,
    Device,
    Telemetry
};
