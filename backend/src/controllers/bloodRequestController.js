const { Op } = require('sequelize');
const { BloodRequest, User, NotificationToken, Donation } = require('../models');
const { getCompatibleDonorGroups, sendEmergencyBloodAlert } = require('../services/firebaseService');

// Auto cleanup helper: Delete notifications/requests older than 2 days (48 hours)
async function autoCleanOldRequests() {
  try {
    const twoDaysAgo = new Date(Date.now() - 48 * 60 * 60 * 1000);
    await BloodRequest.destroy({
      where: {
        createdAt: { [Op.lt]: twoDaysAgo }
      }
    });
  } catch (err) {
    // Non-blocking cleanup
  }
}

// POST /api/blood-requests
const createBloodRequest = async (req, res) => {
  try {
    // Automatically purge old requests (2 days)
    autoCleanOldRequests();

    const {
      requesterName,
      requesterPhone,
      bloodGroup,
      unitsNeeded = 1,
      hospitalName,
      location,
      latitude = 23.8786,
      longitude = 90.3766,
      neededBefore,
      note
    } = req.body;

    if (!requesterPhone || !bloodGroup || !hospitalName || !location) {
      return res.status(400).json({
        success: false,
        message: 'Requester phone, blood group, hospital name, and location are required'
      });
    }

    const cleanName = requesterName || (req.user ? req.user.name : 'Emergency Requester');

    // 1. Save request to PostgreSQL
    const request = await BloodRequest.create({
      requesterName: cleanName,
      requesterPhone: requesterPhone.trim(),
      bloodGroup: bloodGroup.trim().toUpperCase(),
      unitsNeeded: parseInt(unitsNeeded) || 1,
      hospitalName: hospitalName.trim(),
      location: location.trim(),
      latitude: parseFloat(latitude) || 23.8786,
      longitude: parseFloat(longitude) || 90.3766,
      neededBefore: neededBefore || 'জরুরি প্রয়োজন',
      note: note || '',
      status: 'ACTIVE',
      selectedDonorCount: 0
    });

    // 2. Find eligible compatible donors
    const compatibleGroups = getCompatibleDonorGroups(bloodGroup);

    const eligibleDonors = await User.findAll({
      where: {
        isEnabled: true,
        isAvailable: true,
        bloodGroup: { [Op.in]: compatibleGroups },
        phone: { [Op.ne]: requesterPhone.trim() } // Don't notify the requester themselves
      },
      attributes: ['id', 'phone', 'name', 'bloodGroup', 'location', 'fcmToken']
    });

    request.selectedDonorCount = eligibleDonors.length;
    await request.save();

    // 3. Gather tokens for targeted FCM notification
    const donorPhones = eligibleDonors.map(d => d.phone).filter(Boolean);
    const donorIds = eligibleDonors.map(d => d.id).filter(Boolean);

    const notificationTokens = await NotificationToken.findAll({
      where: {
        [Op.or]: [
          { phone: { [Op.in]: donorPhones } },
          { userId: { [Op.in]: donorIds } }
        ]
      },
      attributes: ['token']
    });

    const fcmTokens = [
      ...eligibleDonors.map(d => d.fcmToken).filter(Boolean),
      ...notificationTokens.map(t => t.token).filter(Boolean)
    ];

    // 4. Send targeted emergency alert with deduplication
    let alertResult = { sentCount: 0 };
    if (fcmTokens.length > 0) {
      alertResult = await sendEmergencyBloodAlert(request, fcmTokens);
    }

    return res.status(201).json({
      success: true,
      message: 'Blood request registered successfully and matching donors notified',
      data: request,
      matchSummary: {
        compatibleGroups,
        eligibleDonorsFound: eligibleDonors.length,
        notificationsDispatched: alertResult.sentCount || 0
      }
    });
  } catch (error) {
    console.error('Error in createBloodRequest:', error);
    return res.status(500).json({ success: false, message: 'Failed to create blood request: ' + error.message });
  }
};

// GET /api/blood-requests and GET /admin/blood-requests
const getAllBloodRequests = async (req, res) => {
  try {
    autoCleanOldRequests();

    const { status, bloodGroup, location, search, limit = 50, offset = 0 } = req.query;

    const where = {};
    if (status && status !== 'ALL') {
      where.status = status;
    }
    if (bloodGroup && bloodGroup !== 'ALL') {
      where.bloodGroup = bloodGroup;
    }
    if (location && location !== 'ALL') {
      where.location = { [Op.iLike]: `%${location}%` };
    }
    if (search) {
      where[Op.or] = [
        { requesterName: { [Op.iLike]: `%${search}%` } },
        { hospitalName: { [Op.iLike]: `%${search}%` } },
        { location: { [Op.iLike]: `%${search}%` } },
        { requesterPhone: { [Op.iLike]: `%${search}%` } }
      ];
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

// GET /api/blood-requests/:id
const getBloodRequestById = async (req, res) => {
  try {
    const { id } = req.params;
    const request = await BloodRequest.findByPk(id);

    if (!request) {
      return res.status(404).json({ success: false, message: 'Blood request not found' });
    }

    return res.status(200).json({
      success: true,
      data: request
    });
  } catch (error) {
    console.error('Error in getBloodRequestById:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch request details' });
  }
};

// PATCH /api/blood-requests/:id and PATCH /admin/blood-requests/:id/status
const updateRequestStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status, acceptedDonorName, acceptedDonorPhone, note, requesterConfirmed } = req.body;

    const request = await BloodRequest.findByPk(id);
    if (!request) {
      return res.status(404).json({ success: false, message: 'Blood request not found' });
    }

    const validStatuses = ['PENDING', 'ACTIVE', 'ACCEPTED', 'COMPLETED', 'CANCELLED'];
    if (status) {
      if (!validStatuses.includes(status)) {
        return res.status(400).json({
          success: false,
          message: `Invalid status. Must be one of: ${validStatuses.join(', ')}`
        });
      }
      request.status = status;
    }

    if (acceptedDonorName !== undefined) request.acceptedDonorName = acceptedDonorName;
    if (acceptedDonorPhone !== undefined) request.acceptedDonorPhone = acceptedDonorPhone;
    if (note !== undefined) request.note = note;
    if (requesterConfirmed !== undefined) request.requesterConfirmed = requesterConfirmed;

    await request.save();

    // If status transitioned to COMPLETED and acceptedDonorPhone is set, record in Donations
    if (status === 'COMPLETED' && request.acceptedDonorPhone) {
      const donor = await User.findOne({ where: { phone: request.acceptedDonorPhone } });
      await Donation.create({
        donorId: donor ? donor.id : null,
        donorName: request.acceptedDonorName || (donor ? donor.name : 'Hero Donor'),
        recipientName: request.requesterName,
        hospitalName: request.hospitalName,
        bloodGroup: request.bloodGroup,
        donationDate: new Date(),
        certificateIssued: true
      });

      if (donor) {
        donor.lastDonationDate = new Date();
        await donor.save();
      }
    }

    return res.status(200).json({
      success: true,
      message: `Request status updated to ${request.status}`,
      data: request
    });
  } catch (error) {
    console.error('Error in updateRequestStatus:', error);
    return res.status(500).json({ success: false, message: 'Failed to update request status' });
  }
};

// DELETE /api/blood-requests/:id
const deleteBloodRequest = async (req, res) => {
  try {
    const { id } = req.params;
    const request = await BloodRequest.findByPk(id);

    if (!request) {
      return res.status(404).json({ success: false, message: 'Blood request not found' });
    }

    await request.destroy();
    return res.status(200).json({ success: true, message: 'Blood request deleted successfully' });
  } catch (error) {
    console.error('Error in deleteBloodRequest:', error);
    return res.status(500).json({ success: false, message: 'Failed to delete blood request' });
  }
};

module.exports = {
  createBloodRequest,
  getAllBloodRequests,
  getBloodRequestById,
  updateRequestStatus,
  deleteBloodRequest
};
