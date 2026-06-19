const express = require('express');
const router = express.Router();
const devicesRouter = require('./devices');
const telemetryRouter = require('./telemetry');

router.use('/devices', devicesRouter);
router.use('/devices/:id/telemetry', telemetryRouter);

module.exports = router;
