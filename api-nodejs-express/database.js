const { Sequelize } = require('sequelize');
const { loadEnvFile } = require('node:process');
loadEnvFile();

const sequelize = new Sequelize(process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/postgres', {
  dialect: 'postgres',
  logging: false,
  pool: {
    max: parseInt(process.env.DB_POOL_MAX) || 5,
    min: parseInt(process.env.DB_POOL_MIN) || 0,
    acquire: parseInt(process.env.DB_POOL_ACQUIRE) || 30000,
    idle: parseInt(process.env.DB_POOL_IDLE) || 10000,
  }
});

module.exports = sequelize;
