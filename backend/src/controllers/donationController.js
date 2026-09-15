const { Donation, User } = require('../models');

// POST /api/donations
const createDonation = async (req, res) => {
  try {
    const {
      donorId,
      donorName,
      recipientName,
      hospitalName,
      bloodGroup,
      donationDate,
      certificateIssued = true
    } = req.body;

    if (!recipientName || !hospitalName || !bloodGroup) {
      return res.status(400).json({
        success: false,
        message: 'Recipient name, hospital name, and blood group are required'
      });
    }

    const resolvedDonorName = donorName || (req.user ? req.user.name : 'Generous Donor');
    const resolvedDonorId = donorId || (req.user ? req.user.id : null);

    const donation = await Donation.create({
      donorId: resolvedDonorId,
      donorName: resolvedDonorName,
      recipientName: recipientName.trim(),
      hospitalName: hospitalName.trim(),
      bloodGroup: bloodGroup.trim().toUpperCase(),
      donationDate: donationDate ? new Date(donationDate) : new Date(),
      certificateIssued: certificateIssued === true || certificateIssued === 'true'
    });

    if (resolvedDonorId) {
      await User.update(
        { lastDonationDate: donation.donationDate },
        { where: { id: resolvedDonorId } }
      );
    }

    return res.status(201).json({
      success: true,
      message: 'Donation recorded successfully',
      data: donation
    });
  } catch (error) {
    console.error('Error in createDonation:', error);
    return res.status(500).json({ success: false, message: 'Failed to record donation' });
  }
};

// GET /api/donations and GET /admin/donations
const getDonations = async (req, res) => {
  try {
    const { donorId, bloodGroup, limit = 50, offset = 0 } = req.query;

    const where = {};
    if (donorId) {
      where.donorId = donorId;
    }
    if (bloodGroup && bloodGroup !== 'ALL') {
      where.bloodGroup = bloodGroup;
    }

    const { count, rows } = await Donation.findAndCountAll({
      where,
      limit: parseInt(limit),
      offset: parseInt(offset),
      order: [['donationDate', 'DESC']]
    });

    return res.status(200).json({
      success: true,
      data: {
        total: count,
        donations: rows
      }
    });
  } catch (error) {
    console.error('Error in getDonations:', error);
    return res.status(500).json({ success: false, message: 'Failed to fetch donation records' });
  }
};

// GET /api/donations/:id
const getDonationById = async (req, res) => {
  try {
    const { id } = req.params;
    const donation = await Donation.findByPk(id, {
      include: [{ model: User, as: 'donor', attributes: ['id', 'name', 'phone', 'bloodGroup', 'location'] }]
    });

    if (!donation) {
      return res.status(404).json({ success: false, message: 'Donation record not found' });
    }

    return res.status(200).json({
      success: true,
      data: donation
    });
  } catch (error) {
    console.error('Error in getDonationById:', error);
    return res.status(500).json({ success: false, message: 'Failed to retrieve donation record' });
  }
};

// PATCH /api/donations/:id
const updateDonation = async (req, res) => {
  try {
    const { id } = req.params;
    const donation = await Donation.findByPk(id);

    if (!donation) {
      return res.status(404).json({ success: false, message: 'Donation record not found' });
    }

    const { donorName, recipientName, hospitalName, bloodGroup, certificateIssued } = req.body;

    if (donorName !== undefined) donation.donorName = donorName;
    if (recipientName !== undefined) donation.recipientName = recipientName;
    if (hospitalName !== undefined) donation.hospitalName = hospitalName;
    if (bloodGroup !== undefined) donation.bloodGroup = bloodGroup;
    if (certificateIssued !== undefined) donation.certificateIssued = certificateIssued;

    await donation.save();

    return res.status(200).json({
      success: true,
      message: 'Donation record updated successfully',
      data: donation
    });
  } catch (error) {
    console.error('Error in updateDonation:', error);
    return res.status(500).json({ success: false, message: 'Failed to update donation record' });
  }
};

module.exports = {
  createDonation,
  getDonations,
  getDonationById,
  updateDonation
};
