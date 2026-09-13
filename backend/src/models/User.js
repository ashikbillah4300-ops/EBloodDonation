const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const User = sequelize.define('User', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false
  },
  phone: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true
  },
  bloodGroup: {
    type: DataTypes.STRING(10),
    allowNull: false
  },
  location: {
    type: DataTypes.STRING,
    allowNull: false
  },
  address: {
    type: DataTypes.STRING,
    allowNull: true
  },
  latitude: {
    type: DataTypes.DOUBLE,
    defaultValue: 23.8786
  },
  longitude: {
    type: DataTypes.DOUBLE,
    defaultValue: 90.3766
  },
  isAvailable: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  isEnabled: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  alarmSoundEnabled: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  alarmVibrationEnabled: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  }
}, {
  tableName: 'users',
  timestamps: true
});

module.exports = User;
