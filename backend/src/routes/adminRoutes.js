const express = require('express');
const router = express.Router();
const { adminLogin, getAdminProfile } = require('../controllers/authController');
const { getDashboardStats, getAllUsers, updateUserStatus, deleteUser } = require('../controllers/adminController');
const { getAdminSettings, updateAdminSettings, updateSingleSetting } = require('../controllers/settingsController');
const { getAllBloodRequests, updateRequestStatus } = require('../controllers/bloodRequestController');
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

// Dynamic App Settings (Donation number, notice, maintenance)
router.get('/settings', getAdminSettings);
router.put('/settings', updateAdminSettings);
router.put('/settings/:key', updateSingleSetting);

module.exports = router;
