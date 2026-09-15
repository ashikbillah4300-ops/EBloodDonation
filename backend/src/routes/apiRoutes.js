const express = require('express');
const router = express.Router();
const { registerUser, loginUser, getCurrentUser, adminLogin } = require('../controllers/authController');
const { getUsers, getUserById, updateUser, deleteUser, registerNotificationToken } = require('../controllers/userController');
const {
  createBloodRequest,
  getAllBloodRequests,
  getBloodRequestById,
  updateRequestStatus,
  deleteBloodRequest
} = require('../controllers/bloodRequestController');
const {
  createDonation,
  getDonations,
  getDonationById,
  updateDonation
} = require('../controllers/donationController');
const {
  getPublicSettings,
  updateAdminSettings
} = require('../controllers/settingsController');
const { protectUser, protectAdmin, adminLoginLimiter } = require('../middleware/authMiddleware');

// ================= AUTH ROUTES =================
router.post('/auth/register', registerUser);
router.post('/auth/login', loginUser);
router.get('/auth/me', protectUser, getCurrentUser);

// ================= USER & DONOR ROUTES =================
router.get('/users', getUsers);
router.get('/users/:id', getUserById);
router.patch('/users/:id', updateUser);
router.delete('/users/:id', deleteUser);
router.post('/users/notification-token', registerNotificationToken);
router.post('/notification-token', registerNotificationToken);

// ================= BLOOD REQUESTS ROUTES =================
router.post('/blood-requests', createBloodRequest);
router.get('/blood-requests', getAllBloodRequests);
router.get('/blood-requests/:id', getBloodRequestById);
router.patch('/blood-requests/:id', updateRequestStatus);
router.delete('/blood-requests/:id', deleteBloodRequest);

// ================= DONATIONS ROUTES =================
router.post('/donations', createDonation);
router.get('/donations', getDonations);
router.get('/donations/:id', getDonationById);
router.patch('/donations/:id', updateDonation);

// ================= SETTINGS ROUTES =================
router.get('/settings', getPublicSettings);
router.patch('/settings', updateAdminSettings);
router.put('/settings', updateAdminSettings);

// ================= ADMIN API ALIASES =================
router.post('/admin/login', adminLoginLimiter, adminLogin);

// ================= HEALTH CHECK =================
router.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    online: true,
    service: 'EBloodDonation Cloud API',
    version: '2.0.0',
    timestamp: new Date()
  });
});

module.exports = router;
