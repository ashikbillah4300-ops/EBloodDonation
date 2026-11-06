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

// Normalize incoming path for Vercel Serverless Function
app.use((req, res, next) => {
  // If Vercel invokes function with stripped /api prefix, prepend /api so routes match
  if (!req.url.startsWith('/api') && !req.url.startsWith('/admin') && !req.url.startsWith('/download') && req.url !== '/' && req.url !== '/index.html' && req.url !== '/admin.html') {
    const apiSegments = ['health', 'settings', 'users', 'auth', 'blood-requests', 'donations', 'notification-token'];
    const firstSegment = req.url.split('/')[1]?.split('?')[0];
    if (apiSegments.includes(firstSegment)) {
      req.url = '/api' + req.url;
    }
  }
  next();
});

// Support uploading APK up to 100MB directly via Admin Panel
app.use(express.json({ limit: '100mb' }));
app.use(express.urlencoded({ extended: true, limit: '100mb' }));

// Serve static assets (logos, uploads, icons, styles)
app.use(express.static(path.join(__dirname, '../public')));
app.use('/uploads', express.static(path.join(__dirname, '../public/uploads')));

// Safe cache for static HTML
const loadStaticFile = (fileName) => {
  const candidates = [
    path.join(__dirname, '../public', fileName),
    path.join(__dirname, 'public', fileName),
    path.join(process.cwd(), 'public', fileName),
    path.join(process.cwd(), 'backend/public', fileName),
    path.join(__dirname, '../../backend/public', fileName)
  ];
  for (const p of candidates) {
    try {
      if (fs.existsSync(p)) return fs.readFileSync(p, 'utf8');
    } catch (e) { }
  }
  return null;
};

let cachedIndexHtml = loadStaticFile('index.html');
let cachedAdminHtml = loadStaticFile('admin.html');

// Helper to locate public files safely across different deployment root directories
const getPublicFilePath = (fileName) => {
  const candidates = [
    path.join(__dirname, '../public', fileName),
    path.join(__dirname, '../../backend/public', fileName),
    path.join(process.cwd(), 'public', fileName),
    path.join(process.cwd(), 'backend/public', fileName)
  ];
  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return path.join(__dirname, '../public', fileName);
};

// Serve Public Home Page on Root (/) - NO password required
app.get('/', (req, res) => {
  if (cachedIndexHtml) {
    return res.type('html').send(cachedIndexHtml);
  }
  const file = getPublicFilePath('index.html');
  if (fs.existsSync(file)) {
    return res.sendFile(file);
  }
  res.redirect('/admin');
});

// Serve Admin Control Panel on /admin and /admin-panel
app.get('/admin', (req, res) => {
  if (cachedAdminHtml) {
    return res.type('html').send(cachedAdminHtml);
  }
  res.sendFile(getPublicFilePath('admin.html'));
});

app.get('/admin-panel', (req, res) => {
  if (cachedAdminHtml) {
    return res.type('html').send(cachedAdminHtml);
  }
  res.sendFile(getPublicFilePath('admin.html'));
});

// APK Download Route
app.get('/download/eblood.apk', (req, res) => {
  return res.redirect('https://github.com/ashikbillah4300-ops/EBloodDonation/releases/download/latest/EBloodDonation-debug.apk');
});

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

// Cached database initialization promise for serverless and standalone
let dbInitPromise = null;
const initDatabase = async () => {
  if (dbInitPromise) return dbInitPromise;
  dbInitPromise = (async () => {
    try {
      if (process.env.DATABASE_URL) {
        await sequelize.authenticate();
        console.log('PostgreSQL database connected successfully.');
        await sequelize.sync({ alter: false });
        console.log('Sequelize models synchronized.');
        await seedDefaultAdminIfEmpty();
        await seedDefaultSettingsIfEmpty();
      } else {
        console.warn('DATABASE_URL is not configured; running without persistent DB.');
      }
    } catch (error) {
      console.error('Database connection notice:', error.message);
    }
  })();
  return dbInitPromise;
};

// Ensure DB is initialized before processing API requests
app.use(async (req, res, next) => {
  if (req.path.startsWith('/api') || req.path.startsWith('/admin')) {
    try {
      await initDatabase();
    } catch (err) { }
  }
  next();
});

// Routes
app.use('/admin', adminRoutes);
app.use('/api', apiRoutes);

// Database synchronization and server start for standalone/Render
const startServer = async () => {
  await initDatabase();
  app.listen(PORT, () => {
    console.log(`EBlood Backend & Admin Server running on http://localhost:${PORT}`);
    console.log(`Web Admin Panel accessible at http://localhost:${PORT}/admin-panel`);
  });
};

if (!process.env.VERCEL) {
  startServer();
}

module.exports = app;
