const express = require('express');
const router = express.Router();
const deviceController = require('../controllers/deviceController');
const { validateDevicePayload } = require('../middlewares/validator');

router.get('/', deviceController.getAll);
router.post('/', validateDevicePayload, deviceController.create);
router.get('/:id', deviceController.getById);
router.put('/:id', validateDevicePayload, deviceController.update);
router.delete('/:id', deviceController.delete);

module.exports = router;
