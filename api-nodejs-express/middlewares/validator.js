function validateDevicePayload(req, res, next) {
    const { device_name } = req.body;
    if (!device_name) {
        return res.status(400).json({ error: 'device_name is required' });
    }
    next();
}

module.exports = { validateDevicePayload };
