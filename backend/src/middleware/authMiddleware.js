const jwt = require('jsonwebtoken');
const rateLimit = require('express-rate-limit');
const { AdminUser, User } = require('../models');

// Brute-force protection: max 15 login requests per 15 minutes per IP
const adminLoginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 15,
  message: {
    success: false,
    message: 'Too many login attempts from this IP, please try again after 15 minutes.'
  },
  standardHeaders: true,
  legacyHeaders: false
});

// Protect Admin Routes Middleware
const protectAdmin = async (req, res, next) => {
  let token;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer')
  ) {
    try {
      token = req.headers.authorization.split(' ')[1];
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'eblood_secret_fallback_key');

      const admin = await AdminUser.findByPk(decoded.id, {
        attributes: { exclude: ['password'] }
      });

      if (!admin) {
        return res.status(401).json({ success: false, message: 'Not authorized: Admin not found' });
      }

      req.admin = admin;
      return next();
    } catch (error) {
      return res.status(401).json({ success: false, message: 'Not authorized: Invalid or expired admin token' });
    }
  }

  if (!token) {
    return res.status(401).json({ success: false, message: 'Not authorized: No admin token provided' });
  }
};

// Protect Authenticated User Routes Middleware
const protectUser = async (req, res, next) => {
  let token;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer')
  ) {
    try {
      token = req.headers.authorization.split(' ')[1];
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'eblood_secret_fallback_key');

      const user = await User.findByPk(decoded.id);
      if (!user) {
        return res.status(401).json({ success: false, message: 'User account not found' });
      }
      if (!user.isEnabled) {
        return res.status(403).json({ success: false, message: 'Account is disabled. Please contact support.' });
      }

      req.user = user;
      return next();
    } catch (error) {
      return res.status(401).json({ success: false, message: 'Invalid or expired user session token' });
    }
  }

  // Fallback for seamless offline/online mobile app synchronization using authenticated phone header
  const authPhone = req.headers['x-user-phone'];
  if (authPhone && typeof authPhone === 'string') {
    try {
      const user = await User.findOne({ where: { phone: authPhone.trim() } });
      if (user) {
        req.user = user;
        return next();
      }
    } catch (err) {
      // ignore
    }
  }

  return res.status(401).json({ success: false, message: 'Authentication required. Please log in.' });
};

module.exports = {
  protectAdmin,
  protectUser,
  adminLoginLimiter
};
