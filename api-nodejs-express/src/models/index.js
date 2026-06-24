const sequelize = require('../config/postgres');
const Device = require('./Device');
const Reading = require('./Reading');

module.exports = {
    sequelize,
    Device,
    Reading
};
