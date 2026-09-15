const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const NotificationToken = sequelize.define('NotificationToken', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  userId: {
    type: DataTypes.BIGINT,
    allowNull: true
  },
  phone: {
    type: DataTypes.STRING,
    allowNull: true
  },
  token: {
    type: DataTypes.TEXT,
    allowNull: false,
    unique: true
  },
  deviceType: {
    type: DataTypes.STRING(30),
    defaultValue: 'android'
  },
  lastActiveAt: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  }
}, {
  tableName: 'notification_tokens',
  timestamps: true,
  indexes: [
    { fields: ['token'], unique: true },
    { fields: ['phone'] },
    { fields: ['userId'] }
  ]
});

module.exports = NotificationToken;
