const { Op } = require('sequelize');
const { User, NotificationToken, Donation } = require('../models');

// Haversine distance formula in KM
function calculateDistanceKm(lat1, lon1, lat2, lon2) {
  if (lat1 === undefined || lon1 === undefined || lat2 === undefined || lon2 === undefined) return 9999;
  const R = 6371; // Radius of Earth in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// GET /api/users
const getUsers = async (req, res) => {
  try {
    const {
      bloodGroup,
      location,
      isAvailable,
      search,
      excludePhone,
      excludeUserId,
      latitude,
      longitude,
      nearMe,
      limit = 100,
      offset = 0
    } = req.query;

    const where = { isEnabled: true };

    // Exclude requester's own phone / userId so they never see themselves as a donor
    if (excludePhone && excludePhone.trim().length > 0) {
      where.phone = { [Op.ne]: excludePhone.trim() };
    }
    if (excludeUserId) {
      where.id = { [Op.ne]: excludeUserId };
    }

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

    let { count, rows } = await User.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['createdAt', 'DESC']]
    });

    let usersList = rows.map(u => u.toJSON());

    // Sort by distance if Near Me option or coordinates provided
    const userLat = latitude ? parseFloat(latitude) : (nearMe === 'true' ? 23.8786 : null);
    const userLng = longitude ? parseFloat(longitude) : (nearMe === 'true' ? 90.3766 : null);

    if (userLat !== null && userLng !== null) {
      usersList = usersList.map(u => {
        const dist = calculateDistanceKm(userLat, userLng, parseFloat(u.latitude) || 23.8786, parseFloat(u.longitude) || 90.3766);
        return {
          ...u,
          distanceKm: parseFloat(dist.toFixed(2))
        };
      });

      // Sort strictly in ascending order: closest donor is 1st, 2nd, 3rd...
      usersList.sort((a, b) => a.distanceKm - b.distanceKm);
    }

    return res.status(200).json({
      success: true,
      data: {
        total: count,
        users: usersList
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
