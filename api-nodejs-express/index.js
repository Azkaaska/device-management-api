const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const { sequelize } = require('./models');
const devicesRouter = require('./routes/devices');
const telemetryRouter = require('./routes/telemetry');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.disable('x-powered-by');

// Load Swagger document
const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/docs-ui', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

// Mount routers
app.use('/api/devices', devicesRouter);
app.use('/api/devices', telemetryRouter);

// Sync database and start server
sequelize.sync().then(() => {
    console.log('Database synced');
    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Failed to sync database:', err);
});
