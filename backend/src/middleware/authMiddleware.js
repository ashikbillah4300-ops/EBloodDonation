const jwt = require('jsonwebtoken');
const rateLimit = require('express-rate-limit');
const { AdminUser } = require('../models');

// Brute-force protection: max 5 login requests per 15 minutes per IP
const adminLoginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
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
      next();
    } catch (error) {
      return res.status(401).json({ success: false, message: 'Not authorized: Invalid or expired token' });
    }
  }

  if (!token) {
    return res.status(401).json({ success: false, message: 'Not authorized: No token provided' });
  }
};

module.exports = {
  protectAdmin,
  adminLoginLimiter
};
