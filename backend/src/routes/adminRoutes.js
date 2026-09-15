const express = require('express');
const router = express.Router();
const { adminLogin, getAdminProfile } = require('../controllers/authController');
const { getDashboardStats, getAllUsers, updateUserStatus, deleteUser } = require('../controllers/adminController');
const { getAdminSettings, updateAdminSettings, updateSingleSetting, uploadLogo, resetLogo } = require('../controllers/settingsController');
const { getAllBloodRequests, updateRequestStatus } = require('../controllers/bloodRequestController');
const { getDonations } = require('../controllers/donationController');
const { protectAdmin, adminLoginLimiter } = require('../middleware/authMiddleware');

// Public Admin Auth Route (protected by brute force rate limiter)
router.post('/login', adminLoginLimiter, adminLogin);

// Protected Admin Routes (Requires valid JWT Bearer token)
router.use(protectAdmin);

// Admin Profile
router.get('/me', getAdminProfile);

// Dashboard Statistics
router.get('/dashboard', getDashboardStats);

// User Management
router.get('/users', getAllUsers);
router.patch('/users/:id/status', updateUserStatus);
router.delete('/users/:id', deleteUser);

// Blood Request Management
router.get('/blood-requests', getAllBloodRequests);
router.patch('/blood-requests/:id/status', updateRequestStatus);

// Donations Management
router.get('/donations', getDonations);

// Dynamic App Settings (Donation number, notice, maintenance)
router.get('/settings', getAdminSettings);
router.put('/settings', updateAdminSettings);
router.patch('/settings', updateAdminSettings);
router.put('/settings/:key', updateSingleSetting);

// Logo Management & Upload
router.post('/upload-logo', uploadLogo);
router.post('/reset-logo', resetLogo);

module.exports = router;
