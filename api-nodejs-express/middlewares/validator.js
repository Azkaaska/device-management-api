function validateDevicePayload(req, res, next) {
    const { name, type } = req.body;
    if (!name || !type) {
        return res.status(400).json({ error: 'name and type parameters are required' });
    }
    next();
}

module.exports = { validateDevicePayload };
