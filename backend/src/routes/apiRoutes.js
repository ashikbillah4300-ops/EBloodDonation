const express = require('express');
const router = express.Router();
const { getPublicSettings } = require('../controllers/settingsController');

// Public settings endpoint for Mobile App real-time sync
router.get('/settings', getPublicSettings);

// Health check endpoint for Mobile App ping & connection verification
router.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    online: true,
    service: 'EBloodDonation Cloud API',
    version: '1.0.0',
    timestamp: new Date()
  });
});

module.exports = router;
