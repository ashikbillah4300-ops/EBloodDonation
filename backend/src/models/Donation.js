const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Donation = sequelize.define('Donation', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  donorId: {
    type: DataTypes.BIGINT,
    allowNull: true
  },
  donorName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  recipientName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  hospitalName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  bloodGroup: {
    type: DataTypes.STRING(10),
    allowNull: false
  },
  donationDate: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  },
  certificateIssued: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  }
}, {
  tableName: 'donations',
  timestamps: true
});

module.exports = Donation;
