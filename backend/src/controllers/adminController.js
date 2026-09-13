const { Op } = require('sequelize');
const { User, BloodRequest, Donation, AppSetting } = require('../models');

// GET /admin/dashboard
const getDashboardStats = async (req, res) => {
  try {
    const totalUsers = await User.count();
    const availableDonors = await User.count({ where: { isAvailable: true, isEnabled: true } });
    const totalRequests = await BloodRequest.count();
    const pendingRequests = await BloodRequest.count({ where: { status: 'PENDING' } });
    const activeRequests = await BloodRequest.count({ where: { status: 'ACTIVE' } });
    const completedRequests = await BloodRequest.count({ where: { status: 'COMPLETED' } });
    const totalDonations = await Donation.count();

    const recentUsers = await User.findAll({
      limit: 5,
      order: [['createdAt', 'DESC']]
    });

    const recentRequests = await BloodRequest.findAll({
      limit: 5,
      order: [['createdAt', 'DESC']]
    });

    return res.status(200).json({
      success: true,
      data: {
        summary: {
          totalUsers,
          availableDonors,
          totalRequests,
          pendingRequests,
          activeRequests,
          completedRequests,
          totalDonations
        },
        recentUsers,
        recentRequests
      }
    });
  } catch (error) {
    console.error('Error in getDashboardStats:', error);
    return res.status(500).json({ success: false, message: 'Server error retrieving statistics' });
  }
};

// GET /admin/users
const getAllUsers = async (req, res) => {
  try {
    const { search, bloodGroup, status, limit = 50, offset = 0 } = req.query;

    const where = {};

    if (search) {
      where[Op.or] = [
        { name: { [Op.iLike]: `%${search}%` } },
        { phone: { [Op.iLike]: `%${search}%` } },
        { location: { [Op.iLike]: `%${search}%` } }
      ];
    }

    if (bloodGroup && bloodGroup !== 'ALL') {
      where.bloodGroup = bloodGroup;
    }

    if (status) {
      where.isEnabled = status === 'ACTIVE';
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
    console.error('Error in getAllUsers:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch users' });
  }
};

// PATCH /admin/users/:id/status
const updateUserStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { isEnabled } = req.body;

    const user = await User.findByPk(id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.isEnabled = isEnabled;
    await user.save();

    return res.status(200).json({
      success: true,
      message: `User status changed to ${isEnabled ? 'Active' : 'Disabled'}`,
      data: user
    });
  } catch (error) {
    console.error('Error in updateUserStatus:', error);
    return res.status(500).json({ success: false, message: 'Failed to update user status' });
  }
};

// DELETE /admin/users/:id
const deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    const user = await User.findByPk(id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    await user.destroy();
    return res.status(200).json({ success: true, message: 'User deleted successfully' });
  } catch (error) {
    console.error('Error in deleteUser:', error);
    return res.status(500).json({ success: false, message: 'Failed to delete user' });
  }
};

module.exports = {
  getDashboardStats,
  getAllUsers,
  updateUserStatus,
  deleteUser
};
