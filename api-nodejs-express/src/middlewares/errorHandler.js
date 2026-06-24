const { ValidationError, UniqueConstraintError } = require('sequelize');

function errorHandler(err, req, res, next) {
    console.error(`[Error Handler Exception]: ${err.stack}`);

    if (err instanceof UniqueConstraintError) {
        return res.status(409).json({ error: 'Conflict', detail: err.message });
    }

    if (err instanceof ValidationError) {
        return res.status(400).json({ error: 'Validation error', detail: err.message });
    }

    return res.status(500).json({
        error: 'Internal server error',
        // detail: process.env.NODE_ENV === 'development' ? err.message : 'An unexpected error occurred'
        detail: err.message
    });
}

module.exports = errorHandler;
