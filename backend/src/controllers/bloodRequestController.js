const { BloodRequest } = require('../models');

// GET /admin/blood-requests
const getAllBloodRequests = async (req, res) => {
  try {
    const { status, bloodGroup, limit = 50, offset = 0 } = req.query;

    const where = {};
    if (status && status !== 'ALL') {
      where.status = status;
    }
    if (bloodGroup && bloodGroup !== 'ALL') {
      where.bloodGroup = bloodGroup;
    }

    const { count, rows } = await BloodRequest.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['createdAt', 'DESC']]
    });

    return res.status(200).json({
      success: true,
      data: {
        total: count,
        requests: rows
      }
    });
  } catch (error) {
    console.error('Error in getAllBloodRequests:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch blood requests' });
  }
};

// PATCH /admin/blood-requests/:id/status
const updateRequestStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const validStatuses = ['PENDING', 'ACTIVE', 'ACCEPTED', 'COMPLETED', 'CANCELLED'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        message: `Invalid status. Must be one of: ${validStatuses.join(', ')}`
      });
    }

    const request = await BloodRequest.findByPk(id);
    if (!request) {
      return res.status(404).json({ success: false, message: 'Blood request not found' });
    }

    request.status = status;
    await request.save();

    return res.status(200).json({
      success: true,
      message: `Request status updated to ${status}`,
      data: request
    });
  } catch (error) {
    console.error('Error in updateRequestStatus:', error);
    return res.status(500).json({ success: false, message: 'Failed to update request status' });
  }
};

module.exports = {
  getAllBloodRequests,
  updateRequestStatus
};
