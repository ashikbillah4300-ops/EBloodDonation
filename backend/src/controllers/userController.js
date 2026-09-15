const { Op } = require('sequelize');
const { User, NotificationToken, Donation } = require('../models');

// GET /api/users
const getUsers = async (req, res) => {
  try {
    const { bloodGroup, location, isAvailable, search, limit = 100, offset = 0 } = req.query;

    const where = { isEnabled: true };

    if (bloodGroup && bloodGroup !== 'ALL') {
      where.bloodGroup = bloodGroup;
    }

    if (isAvailable !== undefined && isAvailable !== '') {
      where.isAvailable = isAvailable === 'true' || isAvailable === true;
    }

    if (location && location !== 'ALL') {
      where.location = { [Op.iLike]: `%${location}%` };
    }

    if (search) {
      where[Op.or] = [
        { name: { [Op.iLike]: `%${search}%` } },
        { phone: { [Op.iLike]: `%${search}%` } },
        { location: { [Op.iLike]: `%${search}%` } },
        { address: { [Op.iLike]: `%${search}%` } }
      ];
    }

    const { count, rows } = await User.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['createdAt', 'DESC']]
    });

    return res.status(200).json({
      success: true,
      data: {
        total: count,
        users: rows
      }
    });
  } catch (error) {
    console.error('Error in getUsers:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch donors' });
  }
};

// GET /api/users/:id
const getUserById = async (req, res) => {
  try {
    const { id } = req.params;

    const user = await User.findByPk(id, {
      include: [{ model: Donation, as: 'donations' }]
    });

    if (!user) {
      return res.status(404).json({ success: false, message: 'Donor profile not found' });
    }

    return res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    console.error('Error in getUserById:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch donor profile' });
  }
};

// PATCH /api/users/:id
const updateUser = async (req, res) => {
  try {
    const { id } = req.params;
    const user = await User.findByPk(id);

    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    // Permission check: admin or own profile
    if (req.user && String(req.user.id) !== String(id) && !req.admin) {
      return res.status(403).json({ success: false, message: 'You are not authorized to update this profile' });
    }

    const updatableFields = [
      'name',
      'bloodGroup',
      'location',
      'address',
      'latitude',
      'longitude',
      'isAvailable',
      'isEnabled',
      'alarmSoundEnabled',
      'alarmVibrationEnabled',
      'fcmToken',
      'lastDonationDate',
      'contactMethod'
    ];

    updatableFields.forEach(field => {
      if (req.body[field] !== undefined) {
        user[field] = req.body[field];
      }
    });

    await user.save();

    // If fcmToken is updated, save to NotificationToken table as well
    if (req.body.fcmToken && req.body.fcmToken.length > 10) {
      await NotificationToken.upsert({
        userId: user.id,
        phone: user.phone,
        token: req.body.fcmToken.trim(),
        lastActiveAt: new Date()
      });
    }

    return res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      data: user
    });
  } catch (error) {
    console.error('Error in updateUser:', error);
    return res.status(500).json({ success: false, message: 'Failed to update profile' });
  }
};

// DELETE /api/users/:id
const deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    const user = await User.findByPk(id);

    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (req.user && String(req.user.id) !== String(id) && !req.admin) {
      return res.status(403).json({ success: false, message: 'Unauthorized to delete this account' });
    }

    await NotificationToken.destroy({ where: { userId: user.id } });
    await user.destroy();

    return res.status(200).json({
      success: true,
      message: 'User account removed successfully'
    });
  } catch (error) {
    console.error('Error in deleteUser:', error);
    return res.status(500).json({ success: false, message: 'Failed to delete user' });
  }
};

// POST /api/users/notification-token
const registerNotificationToken = async (req, res) => {
  try {
    const { token, phone, deviceType = 'android' } = req.body;

    if (!token || token.trim().length < 10) {
      return res.status(400).json({ success: false, message: 'Valid FCM token is required' });
    }

    const userId = req.user ? req.user.id : null;
    const userPhone = phone || (req.user ? req.user.phone : null);

    const [record] = await NotificationToken.upsert({
      userId,
      phone: userPhone,
      token: token.trim(),
      deviceType,
      lastActiveAt: new Date()
    });

    if (userId) {
      await User.update({ fcmToken: token.trim() }, { where: { id: userId } });
    }

    return res.status(200).json({
      success: true,
      message: 'Notification token registered successfully',
      data: record
    });
  } catch (error) {
    console.error('Error in registerNotificationToken:', error);
    return res.status(500).json({ success: false, message: 'Failed to register token' });
  }
};

module.exports = {
  getUsers,
  getUserById,
  updateUser,
  deleteUser,
  registerNotificationToken
};
