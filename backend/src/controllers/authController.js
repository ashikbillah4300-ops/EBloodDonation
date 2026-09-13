const jwt = require('jsonwebtoken');
const { AdminUser } = require('../models');

const generateToken = (id, role) => {
  return jwt.sign({ id, role }, process.env.JWT_SECRET || 'eblood_secret_fallback_key', {
    expiresIn: process.env.JWT_EXPIRES_IN || '7d'
  });
};

// POST /admin/login
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
        [require('sequelize').Op.or]: [
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

    const token = generateToken(admin.id, admin.role);

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
  adminLogin,
  getAdminProfile
};
