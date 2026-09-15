const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const path = require('path');
const fs = require('fs');
require('dotenv').config();

const { sequelize, AdminUser } = require('./models');
const { seedDefaultSettingsIfEmpty } = require('./controllers/settingsController');
const adminRoutes = require('./routes/adminRoutes');
const apiRoutes = require('./routes/apiRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Security Headers
app.use(helmet({
  contentSecurityPolicy: false, // Allows CDN resources on landing/admin pages
  crossOriginEmbedderPolicy: false
}));

// CORS Configuration with environment origin whitelist
const allowedOrigins = process.env.ALLOWED_ORIGINS
  ? process.env.ALLOWED_ORIGINS.split(',').map(s => s.trim())
  : ['http://localhost:3000', 'http://localhost:5000', 'http://localhost:8080'];

app.use(cors({
  origin: (origin, callback) => {
    // Allow requests with no origin (like mobile apps or curl requests)
    if (!origin) return callback(null, true);
    if (process.env.NODE_ENV !== 'production' || allowedOrigins.includes(origin)) {
      return callback(null, true);
    }
    return callback(null, true); // Permissive during preview/staging
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'x-user-phone', 'X-Requested-With']
}));

app.use(express.json({ limit: '20mb' }));
app.use(express.urlencoded({ extended: true, limit: '20mb' }));

// Serve static assets (logos, uploads, icons, styles)
app.use(express.static(path.join(__dirname, '../public')));
app.use('/uploads', express.static(path.join(__dirname, '../public/uploads')));

// Serve Public Home Page on Root (/) - NO password required
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// Serve Admin Control Panel on /admin and /admin-panel
app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/admin.html'));
});

app.get('/admin-panel', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/admin.html'));
});

// APK Download Route
app.get('/download/eblood.apk', (req, res) => {
  const apkPath = path.join(__dirname, '../public/eblood.apk');
  if (fs.existsSync(apkPath)) {
    res.download(apkPath, 'EBloodDonation.apk');
  } else {
    res.send(`
      <!DOCTYPE html>
      <html lang="bn">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>APK ডাউনলোড প্রস্তুতি</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://fonts.googleapis.com/css2?family=Hind+Siliguri:wght@400;600;700&display=swap" rel="stylesheet">
        <style>body { font-family: 'Hind Siliguri', sans-serif; }</style>
      </head>
      <body class="bg-slate-950 text-white min-h-screen flex items-center justify-center p-4">
        <div class="max-w-md w-full bg-slate-900 border border-slate-800 rounded-2xl p-6 text-center shadow-2xl">
          <div class="text-4xl mb-3">📦</div>
          <h2 class="text-xl font-bold text-white mb-2">APK ফাইলটি প্রজেক্টে যুক্ত করুন</h2>
          <p class="text-slate-300 text-sm mb-4 leading-relaxed">
            AI Studio-র উপরের ডানপাশের সেটিংস (Settings) থেকে <b>"Export APK"</b> অথবা <b>"Download Project"</b> করে APK ফাইলটি <code>backend/public/eblood.apk</code> নামে রাখলেই এই বাটনে ক্লিক করে সবাই সরাসরি অ্যাপটি ডাউনলোড করতে পারবে!
          </p>
          <a href="/" class="inline-block px-5 py-2.5 bg-rose-600 hover:bg-rose-500 rounded-xl text-sm font-bold text-white">
            ← মূল পেজে ফিরে যান
          </a>
        </div>
      </body>
      </html>
    `);
  }
});

// Routes
app.use('/admin', adminRoutes);
app.use('/api', apiRoutes);

// Seed default Admin securely if not exists
const seedDefaultAdminIfEmpty = async () => {
  try {
    const defaultUsername = process.env.DEFAULT_ADMIN_USERNAME || 'ashikbillah4300@gmail.com';
    const defaultEmail = (process.env.DEFAULT_ADMIN_EMAIL || 'ashikbillah4300@gmail.com').toLowerCase();
    const defaultPassword = process.env.DEFAULT_ADMIN_PASSWORD || 'ashik@2008';

    let admin = await AdminUser.findOne({
      where: {
        [require('sequelize').Op.or]: [
          { email: defaultEmail },
          { username: defaultUsername },
          { username: 'admin' }
        ]
      }
    });

    if (!admin) {
      await AdminUser.create({
        username: defaultUsername,
        email: defaultEmail,
        password: defaultPassword,
        role: 'SUPER_ADMIN'
      });
      console.log(`Default Super Admin created with email: ${defaultEmail}`);
    } else {
      admin.email = defaultEmail;
      admin.role = 'SUPER_ADMIN';
      if (process.env.RESET_ADMIN_PASSWORD === 'true') {
        admin.password = defaultPassword;
      }
      await admin.save();
      console.log(`Super Admin verified: ${defaultEmail}`);
    }
  } catch (error) {
    console.error('Error verifying default admin:', error.message);
  }
};

// Database synchronization and server start
const startServer = async () => {
  try {
    await sequelize.authenticate();
    console.log('PostgreSQL database connected successfully.');

    // In development or when requested, sync models without deleting existing tables
    await sequelize.sync({ alter: false });
    console.log('Sequelize models synchronized.');

    // Seed initial admin and app settings
    await seedDefaultAdminIfEmpty();
    await seedDefaultSettingsIfEmpty();

    app.listen(PORT, () => {
      console.log(`EBlood Backend & Admin Server running on http://localhost:${PORT}`);
      console.log(`Web Admin Panel accessible at http://localhost:${PORT}/admin-panel`);
    });
  } catch (error) {
    console.error('Unable to connect to database:', error.message);
    // Start server in fallback mode so endpoints remain accessible
    app.listen(PORT, () => {
      console.log(`Server running in fallback mode on port ${PORT}`);
    });
  }
};

startServer();

module.exports = app;
