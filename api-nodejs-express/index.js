const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');

const { sequelize } = require('./models');
const apiRouter = require('./routes');
const errorHandler = require('./middlewares/errorHandler');
const { bootstrapMqttWorker } = require('./workers/mqttIngestion');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.disable('x-powered-by');

const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/docs-ui', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use('/api/v1', apiRouter);

app.use(errorHandler);

sequelize.sync()
    .then(() => {
        console.log('PostgreSQL Database synchronized successfully.');

        bootstrapMqttWorker();

        app.listen(PORT, () => {
            console.log(`Express HTTP Application cluster running on port ${PORT}`);
        });
    })
    .catch(err => {
        console.error('CRITICAL: Failed to synchronize engine modules:', err);
        process.exit(1);
    });
