const { sequelize } = require('../config/database');
const User = require('./User');
const BloodRequest = require('./BloodRequest');
const Donation = require('./Donation');
const AdminUser = require('./AdminUser');
const AppSetting = require('./AppSetting');

// Relations
User.hasMany(Donation, { foreignKey: 'donorId', as: 'donations' });
Donation.belongsTo(User, { foreignKey: 'donorId', as: 'donor' });

module.exports = {
  sequelize,
  User,
  BloodRequest,
  Donation,
  AdminUser,
  AppSetting
};
