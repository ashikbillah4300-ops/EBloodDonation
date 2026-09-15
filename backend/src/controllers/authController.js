const jwt = require('jsonwebtoken');
const { Op } = require('sequelize');
const { AdminUser, User, NotificationToken } = require('../models');
const { verifyFirebaseIdToken } = require('../services/firebaseService');

const generateToken = (id, role = 'USER') => {
  return jwt.sign({ id, role }, process.env.JWT_SECRET || 'eblood_secret_fallback_key', {
    expiresIn: process.env.JWT_EXPIRES_IN || '30d'
  });
};

// Normalize Bangladeshi phone number
const normalizePhone = (input) => {
  if (!input) return '';
  const digits = input.replace(/\D/g, '');
  if (digits.startsWith('880')) {
    return '0' + digits.substring(3);
  }
  if (digits.startsWith('0')) {
    return digits;
  }
  return '0' + digits;
};

// POST /api/auth/register
const registerUser = async (req, res) => {
  try {
    const {
      name,
      phone,
      bloodGroup = 'A+',
      location = 'Dhaka',
      address,
      latitude = 23.8786,
      longitude = 90.3766,
      fcmToken,
      firebaseToken
    } = req.body;

    if (!phone || !phone.trim()) {
      return res.status(400).json({ success: false, message: 'Phone number is required' });
    }

    const cleanPhone = normalizePhone(phone);
    const cleanName = (name && name.trim().length > 0) ? name.trim() : `Donor ${cleanPhone.slice(-4)}`;

    // Optional Firebase token verification if present
    if (firebaseToken) {
      const decoded = await verifyFirebaseIdToken(firebaseToken);
      if (decoded && decoded.phone_number) {
        // Verified Firebase Phone Auth
      }
    }

    // Upsert user
    let [user, created] = await User.findOrCreate({
      where: { phone: cleanPhone },
      defaults: {
        name: cleanName,
        phone: cleanPhone,
        bloodGroup: bloodGroup || 'A+',
        location: location || 'Dhaka',
        address: address || '',
        latitude: latitude || 23.8786,
        longitude: longitude || 90.3766,
        fcmToken: fcmToken || null,
        isAvailable: true,
        isEnabled: true
      }
    });

    if (!created) {
      if (cleanName) user.name = cleanName;
      if (bloodGroup) user.bloodGroup = bloodGroup;
      if (location) user.location = location;
      if (address !== undefined) user.address = address;
      if (latitude) user.latitude = latitude;
      if (longitude) user.longitude = longitude;
      if (fcmToken) user.fcmToken = fcmToken;
      user.isEnabled = true;
      await user.save();
    }

    // Register FCM notification token if provided
    if (fcmToken && fcmToken.trim().length > 10) {
      await NotificationToken.upsert({
        userId: user.id,
        phone: user.phone,
        token: fcmToken.trim(),
        lastActiveAt: new Date()
      });
    }

    const token = generateToken(user.id, 'USER');

    return res.status(created ? 201 : 200).json({
      success: true,
      message: created ? 'User account registered successfully' : 'User account updated successfully',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    console.error('Error in registerUser:', error);
    return res.status(500).json({ success: false, message: 'Failed to complete registration' });
  }
};

// POST /api/auth/login
const loginUser = async (req, res) => {
  try {
    const { phone, firebaseToken, fcmToken } = req.body;

    if (!phone || !phone.trim()) {
      return res.status(400).json({ success: false, message: 'Phone number is required' });
    }

    const cleanPhone = normalizePhone(phone);

    // Optional Firebase token check
    if (firebaseToken) {
      await verifyFirebaseIdToken(firebaseToken);
    }

    let user = await User.findOne({ where: { phone: cleanPhone } });

    if (!user) {
      return res.status(404).json({
        success: false,
        notRegistered: true,
        message: 'No account found with this phone number. Please complete registration.'
      });
    }

    if (!user.isEnabled) {
      return res.status(403).json({
        success: false,
        message: 'Your account has been deactivated by administrator. Please contact support.'
      });
    }

    if (fcmToken && fcmToken.trim().length > 10) {
      user.fcmToken = fcmToken.trim();
      await user.save();

      await NotificationToken.upsert({
        userId: user.id,
        phone: user.phone,
        token: fcmToken.trim(),
        lastActiveAt: new Date()
      });
    }

    const token = generateToken(user.id, 'USER');

    return res.status(200).json({
      success: true,
      message: 'Logged in successfully',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    console.error('Error in loginUser:', error);
    return res.status(500).json({ success: false, message: 'Authentication failed' });
  }
};

// GET /api/auth/me
const getCurrentUser = async (req, res) => {
  try {
    return res.status(200).json({
      success: true,
      data: req.user
    });
  } catch (error) {
    console.error('Error in getCurrentUser:', error);
    return res.status(500).json({ success: false, message: 'Failed to retrieve profile' });
  }
};

// POST /admin/login and POST /api/admin/login
const adminLogin = async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: 'Username/Email and password are required'
      });
    }

    // Find admin by username or email
    const admin = await AdminUser.findOne({
      where: {
        [Op.or]: [
          { username: username.trim() },
          { email: username.trim().toLowerCase() }
        ]
      }
    });

    if (!admin) {
      return res.status(401).json({
        success: false,
        message: 'Invalid administrative credentials'
      });
    }

    const isMatch = await admin.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid administrative credentials'
      });
    }

    // Update last login timestamp
    admin.lastLoginAt = new Date();
    await admin.save();

    const token = generateToken(admin.id, admin.role || 'SUPER_ADMIN');

    return res.status(200).json({
      success: true,
      message: 'Admin authenticated successfully',
      data: {
        id: admin.id,
        username: admin.username,
        email: admin.email,
        role: admin.role,
        token
      }
    });
  } catch (error) {
    console.error('Error in adminLogin:', error);
    return res.status(500).json({
      success: false,
      message: 'Internal server error during authentication'
    });
  }
};

// GET /admin/me
const getAdminProfile = async (req, res) => {
  return res.status(200).json({
    success: true,
    data: req.admin
  });
};

module.exports = {
  registerUser,
  loginUser,
  getCurrentUser,
  adminLogin,
  getAdminProfile
};
