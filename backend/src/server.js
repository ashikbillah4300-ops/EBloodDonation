const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const { sequelize, AdminUser } = require('./models');
const { seedDefaultSettingsIfEmpty } = require('./controllers/settingsController');
const adminRoutes = require('./routes/adminRoutes');
const apiRoutes = require('./routes/apiRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve Admin Panel Web Dashboard statically
app.use('/admin-panel', express.static(path.join(__dirname, '../public')));
app.get('/admin', (req, res) => {
  res.redirect('/admin-panel');
});

// Routes
app.use('/admin', adminRoutes);
app.use('/api', apiRoutes);

// Seed default Admin if not exists
const seedDefaultAdminIfEmpty = async () => {
  try {
    const adminCount = await AdminUser.count();
    if (adminCount === 0) {
      const defaultUsername = process.env.DEFAULT_ADMIN_USERNAME || 'admin';
      const defaultEmail = process.env.DEFAULT_ADMIN_EMAIL || 'admin@eblood.org';
      const defaultPassword = process.env.DEFAULT_ADMIN_PASSWORD || 'eblood@2026';

      await AdminUser.create({
        username: defaultUsername,
        email: defaultEmail,
        password: defaultPassword,
        role: 'SUPER_ADMIN'
      });
      console.log(`Default Super Admin created: ${defaultUsername} / (Password: from env or eblood@2026)`);
    }
  } catch (error) {
    console.error('Error seeding default admin:', error);
  }
};

// Database synchronization and server start
const startServer = async () => {
  try {
    await sequelize.authenticate();
    console.log('PostgreSQL database connected successfully.');

    // Sync database models (alter: true creates/updates tables without dropping data)
    await sequelize.sync({ alter: true });
    console.log('Sequelize models synchronized.');

    // Seed initial admin and app settings
    await seedDefaultAdminIfEmpty();
    await seedDefaultSettingsIfEmpty();

    app.listen(PORT, () => {
      console.log(`EBlood Backend & Admin Server running on http://localhost:${PORT}`);
      console.log(`Web Admin Panel accessible at http://localhost:${PORT}/admin-panel`);
    });
  } catch (error) {
    console.error('Unable to connect to database:', error);
    // Even if db is offline initially, start server so mock/offline endpoints can run
    app.listen(PORT, () => {
      console.log(`Server started in offline/fallback mode on port ${PORT}`);
    });
  }
};

startServer();

module.exports = app;
