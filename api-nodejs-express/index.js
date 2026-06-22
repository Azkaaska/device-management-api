const express = require('express');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const http = require('http');

const { sequelize } = require('./models');
const apiRouter = require('./routes');
const errorHandler = require('./middlewares/errorHandler');
const { bootstrapMqttWorker } = require('./workers/mqttIngestion');
const { initWebSocketServer } = require('./services/websocketService');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.disable('x-powered-by');

app.use(express.static(path.join(__dirname, 'public')));

const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
app.use('/docs-ui', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use('/api/v1', apiRouter);

app.use(errorHandler);

sequelize.sync()
    .then(() => {
        console.log('PostgreSQL Database synchronized successfully.');
        
        bootstrapMqttWorker();

        const server = http.createServer(app);        
        initWebSocketServer(server);

        server.listen(PORT, () => {
            console.log(`Express HTTP Application cluster running on port ${PORT}`);
        });
    })
    .catch(err => {
        console.error('CRITICAL: Failed to synchronize engine modules:', err);
        process.exit(1);
    });
