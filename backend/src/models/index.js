const { sequelize } = require('../config/database');
const User = require('./User');
const BloodRequest = require('./BloodRequest');
const Donation = require('./Donation');
const AdminUser = require('./AdminUser');
const AppSetting = require('./AppSetting');
const NotificationToken = require('./NotificationToken');

// Relations
User.hasMany(Donation, { foreignKey: 'donorId', as: 'donations' });
Donation.belongsTo(User, { foreignKey: 'donorId', as: 'donor' });
User.hasMany(NotificationToken, { foreignKey: 'userId', as: 'notificationTokens' });
NotificationToken.belongsTo(User, { foreignKey: 'userId', as: 'user' });

module.exports = {
  sequelize,
  User,
  BloodRequest,
  Donation,
  AdminUser,
  AppSetting,
  NotificationToken
};
