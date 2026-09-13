const { AppSetting } = require('../models');

// Default initial settings
const DEFAULT_SETTINGS = [
  { setting_key: 'donation_number', setting_value: '01969114300', description: 'Personal bKash send money number' },
  { setting_key: 'deposit_method', setting_value: 'bKash', description: 'Selected Deposit/Payment Gateway (bKash / Nagad / Rocket)' },
  { setting_key: 'deposit_number', setting_value: '01969114300', description: 'Active deposit account number' },
  { setting_key: 'contact_number', setting_value: '+8801969114300', description: 'Public contact phone' },
  { setting_key: 'support_number', setting_value: '+8801969114300', description: 'Support helpline' },
  { setting_key: 'app_notice', setting_value: 'Welcome to EBloodDonation. Save lives by donating blood regularly!', description: 'General notice banner' },
  { setting_key: 'emergency_notice', setting_value: '', description: 'Crisis urgent red announcement banner' },
  { setting_key: 'maintenance_mode', setting_value: 'false', description: 'Enable maintenance mode' },
  { setting_key: 'minimum_app_version', setting_value: '1.0.0', description: 'Enforce app updates' }
];

// Seed default settings if table is empty
const seedDefaultSettingsIfEmpty = async () => {
  try {
    const count = await AppSetting.count();
    if (count === 0) {
      await AppSetting.bulkCreate(DEFAULT_SETTINGS);
      console.log('App settings initialized with defaults.');
    }
  } catch (error) {
    console.error('Error seeding app settings:', error);
  }
};

// GET /api/settings (Public - for Android Mobile App live sync)
const getPublicSettings = async (req, res) => {
  try {
    const settings = await AppSetting.findAll();
    const settingsMap = {};
    settings.forEach(s => {
      settingsMap[s.setting_key] = s.setting_value;
    });

    return res.status(200).json({
      success: true,
      data: settingsMap
    });
  } catch (error) {
    console.error('Error in getPublicSettings (serving fallback defaults):', error.message);
    const fallbackMap = {};
    DEFAULT_SETTINGS.forEach(s => {
      fallbackMap[s.setting_key] = s.setting_value;
    });
    return res.status(200).json({
      success: true,
      data: fallbackMap,
      isFallback: true
    });
  }
};

// GET /admin/settings (Admin Protected)
const getAdminSettings = async (req, res) => {
  try {
    const settings = await AppSetting.findAll({
      order: [['setting_key', 'ASC']]
    });
    return res.status(200).json({
      success: true,
      data: settings
    });
  } catch (error) {
    console.error('Error in getAdminSettings:', error);
    return res.status(500).json({ success: false, message: 'Failed to retrieve settings' });
  }
};

// PUT /admin/settings (Admin Protected - bulk or multiple updates)
const updateAdminSettings = async (req, res) => {
  try {
    const { settings } = req.body; // e.g. { donation_number: "019...", app_notice: "..." }

    if (!settings || typeof settings !== 'object') {
      return res.status(400).json({ success: false, message: 'Invalid settings payload' });
    }

    for (const [key, val] of Object.entries(settings)) {
      await AppSetting.upsert({
        setting_key: key,
        setting_value: String(val)
      });
    }

    return res.status(200).json({
      success: true,
      message: 'Settings updated successfully! Mobile application will reflect changes in real time.',
      data: settings
    });
  } catch (error) {
    console.error('Error in updateAdminSettings:', error);
    return res.status(500).json({ success: false, message: 'Failed to update settings' });
  }
};

// PUT /admin/settings/:key (Admin Protected - single update)
const updateSingleSetting = async (req, res) => {
  try {
    const { key } = req.params;
    const { value } = req.body;

    if (value === undefined) {
      return res.status(400).json({ success: false, message: 'Value is required' });
    }

    await AppSetting.upsert({
      setting_key: key,
      setting_value: String(value)
    });

    return res.status(200).json({
      success: true,
      message: `Setting '${key}' updated successfully.`,
      data: { setting_key: key, setting_value: value }
    });
  } catch (error) {
    console.error('Error in updateSingleSetting:', error);
    return res.status(500).json({ success: false, message: 'Failed to update setting' });
  }
};

module.exports = {
  seedDefaultSettingsIfEmpty,
  getPublicSettings,
  getAdminSettings,
  updateAdminSettings,
  updateSingleSetting
};
