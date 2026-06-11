const sequelize = require('../database');
const Device = require('./Device');
const Reading = require('./Reading');

// Define Associations (ORM level, no actual DB constraint required)
Device.hasMany(Reading, { foreignKey: 'device_id', onDelete: 'CASCADE' });
Reading.belongsTo(Device, { foreignKey: 'device_id' });
module.exports = {
    sequelize,
    Device,
    Reading
};
