const fs = require('fs');
const path = require('path');
const { AppSetting } = require('../models');

// Default initial settings (Categorized into App Control and Website Control)
const DEFAULT_SETTINGS = [
  // 1. Mobile App Controls
  { setting_key: 'app_logo_url', setting_value: '/logo.svg', description: 'Application brand logo URL' },
  { setting_key: 'app_name', setting_value: 'EBlood Donation', description: 'Application display name' },
  { setting_key: 'donation_number', setting_value: '01969114300', description: 'Personal bKash send money number' },
  { setting_key: 'deposit_method', setting_value: 'bKash', description: 'Selected Deposit/Payment Gateway (bKash / Nagad / Rocket)' },
  { setting_key: 'deposit_number', setting_value: '01969114300', description: 'Active deposit account number' },
  { setting_key: 'contact_number', setting_value: '+8801969114300', description: 'Public contact phone' },
  { setting_key: 'support_number', setting_value: '+8801969114300', description: 'Support helpline' },
  { setting_key: 'app_notice', setting_value: 'Welcome to EBloodDonation. Save lives by donating blood regularly!', description: 'General notice banner' },
  { setting_key: 'emergency_notice', setting_value: '', description: 'Crisis urgent red announcement banner' },
  { setting_key: 'maintenance_mode', setting_value: 'false', description: 'Enable app maintenance mode' },
  { setting_key: 'minimum_app_version', setting_value: '1.0.0', description: 'Enforce app updates' },
  { setting_key: 'app_sos_alarm_enabled', setting_value: 'true', description: 'Enable emergency SOS audible alarm broadcast' },

  // 2. Website Controls
  { setting_key: 'website_title', setting_value: 'EBlood Donation — রক্তদান ও সেবা প্ল্যাটফর্ম', description: 'Website title & branding' },
  { setting_key: 'website_hero_title', setting_value: 'এক ক্লিকেই রক্তদাতা খুঁজুন, বাঁচান একটি মূল্যবান জীবন', description: 'Hero main headline on website' },
  { setting_key: 'website_hero_subtitle', setting_value: 'আপনার এরিয়ার ভেরিফায়েড রক্তদাতা, রিয়েল-টাইম ব্লাড রিকোয়েস্ট, লাইভ হসপিটাল ডিরেক্টরি এবং ২৪/৭ জরুরি হটলাইন সার্ভিস — সবই এখন একটি প্ল্যাটফর্মে।', description: 'Hero subtitle on website' },
  { setting_key: 'website_announcement', setting_value: 'জরুরি রক্তদান ও তাৎক্ষণিক ডোনার খোঁজার নির্ভরযোগ্য প্ল্যাটফর্ম', description: 'Top announcement badge on website' },
  { setting_key: 'website_helpline', setting_value: '+8801969114300', description: 'Website support hotline number' },
  { setting_key: 'website_support_email', setting_value: 'support@eblood.org', description: 'Website support email address' },
  { setting_key: 'website_apk_version', setting_value: 'v1.0 Live APK', description: 'Displayed APK version badge' },
  { setting_key: 'website_show_public_donors', setting_value: 'true', description: 'Show public donor directory on website' },
  { setting_key: 'website_maintenance', setting_value: 'false', description: 'Put website in maintenance mode' },
  { setting_key: 'website_footer_text', setting_value: '© 2026 EBlood Donation. সকল অধিকার সংরক্ষিত। মানবিক সেবায় নিবেদিত।', description: 'Website footer text' }
];

// Seed default settings if table is empty or ensure all keys exist
const seedDefaultSettingsIfEmpty = async () => {
  try {
    for (const item of DEFAULT_SETTINGS) {
      await AppSetting.findOrCreate({
        where: { setting_key: item.setting_key },
        defaults: item
      });
    }
    console.log('App and Website settings successfully synchronized with defaults.');
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

// POST /admin/upload-logo (Admin Protected)
const uploadLogo = async (req, res) => {
  try {
    const { imageBase64, imageUrl } = req.body;
    let finalLogoUrl = '';

    const uploadsDir = path.join(__dirname, '../../public/uploads');
    if (!fs.existsSync(uploadsDir)) {
      fs.mkdirSync(uploadsDir, { recursive: true });
    }

    if (imageBase64) {
      // imageBase64 format: data:image/png;base64,...
      const matches = imageBase64.match(/^data:([A-Za-z0-9\/\+\-\.]+);base64,(.+)$/);
      if (!matches || matches.length !== 3) {
        return res.status(400).json({ success: false, message: 'Invalid image data. Please select a valid PNG, JPG, or SVG file.' });
      }

      const mimeType = matches[1].toLowerCase();
      const base64Data = matches[2];
      const buffer = Buffer.from(base64Data, 'base64');

      let ext = 'png';
      if (mimeType.includes('svg')) ext = 'svg';
      else if (mimeType.includes('jpeg') || mimeType.includes('jpg')) ext = 'jpg';
      else if (mimeType.includes('webp')) ext = 'webp';

      const filename = `custom_logo_${Date.now()}.${ext}`;
      const filePath = path.join(uploadsDir, filename);
      fs.writeFileSync(filePath, buffer);

      // Also copy to canonical active_logo for consistent caching
      const canonicalPath = path.join(uploadsDir, `active_logo.${ext}`);
      fs.writeFileSync(canonicalPath, buffer);

      finalLogoUrl = `/uploads/${filename}`;
    } else if (imageUrl && typeof imageUrl === 'string' && imageUrl.trim().length > 0) {
      finalLogoUrl = imageUrl.trim();
    } else {
      return res.status(400).json({ success: false, message: 'No image file or URL was provided.' });
    }

    // Persist to database
    await AppSetting.upsert({
      setting_key: 'app_logo_url',
      setting_value: finalLogoUrl,
      description: 'Active application brand logo'
    });

    return res.status(200).json({
      success: true,
      message: 'Logo updated successfully! All screens, landing pages, and the mobile app will now display the new logo.',
      logoUrl: finalLogoUrl
    });
  } catch (error) {
    console.error('Error uploading logo:', error);
    return res.status(500).json({ success: false, message: 'Failed to upload logo: ' + error.message });
  }
};

// POST /admin/reset-logo (Admin Protected)
const resetLogo = async (req, res) => {
  try {
    const defaultUrl = '/logo.svg';
    await AppSetting.upsert({
      setting_key: 'app_logo_url',
      setting_value: defaultUrl,
      description: 'Active application brand logo'
    });

    return res.status(200).json({
      success: true,
      message: 'Logo reset to default official EBloodDonation logo.',
      logoUrl: defaultUrl
    });
  } catch (error) {
    console.error('Error resetting logo:', error);
    return res.status(500).json({ success: false, message: 'Failed to reset logo' });
  }
};

module.exports = {
  seedDefaultSettingsIfEmpty,
  getPublicSettings,
  getAdminSettings,
  updateAdminSettings,
  updateSingleSetting,
  uploadLogo,
  resetLogo
};
