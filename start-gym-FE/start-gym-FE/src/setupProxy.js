const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
    // Only proxy /api requests to the backend
    app.use(
        '/api',
        createProxyMiddleware({
            target: 'http://localhost:8080', // Backend server address
            changeOrigin: true,
        })
    );

    // Add a simple middleware to prevent proxying /assets/ requests to backend
    // This prevents authentication errors for static assets
    app.use((req, res, next) => {
        if (req.url.startsWith('/assets/')) {
            console.log('Asset request blocked from proxying:', req.url);
            return res.status(204).end(); // Return empty response to prevent further processing
        }
        next();
    });
};