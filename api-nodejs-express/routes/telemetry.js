const express = require('express');
const router = express.Router({ mergeParams: true }); 
const telemetryController = require('../controllers/telemetryController');

router.get('/', telemetryController.getTelemetry);
router.post('/', telemetryController.postTelemetry);

module.exports = router;
