const { Sequelize } = require('sequelize');

const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: ':memory:',
  logging: false
});

// Optimize SQLite for performance and enable foreign keys
sequelize.beforeConnect(async (config) => {
    // This hook might not fire for sqlite memory exactly, but let's use afterConnect instead.
});

// Use afterConnect to run PRAGMAs once connected
sequelize.addHook('afterConnect', async (connection) => {
    return new Promise((resolve, reject) => {
        connection.run('PRAGMA foreign_keys=ON', (err) => {
            if (err) return reject(err);
            connection.run('PRAGMA synchronous=OFF', (err) => {
                if (err) return reject(err);
                connection.run('PRAGMA journal_mode=MEMORY', (err) => {
                    if (err) return reject(err);
                    resolve();
                });
            });
        });
    });
});

module.exports = sequelize;
