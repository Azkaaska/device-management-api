const { Sequelize } = require('sequelize');
const { loadEnvFile } = require('node:process');
loadEnvFile();

const sequelize = new Sequelize(process.env.DATABASE_URL || 'postgresql://user:password@localhost:5432/database', {
  dialect: 'postgres',
  logging: false
});

module.exports = sequelize;
