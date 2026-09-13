const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const BloodRequest = sequelize.define('BloodRequest', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true
  },
  requesterName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  requesterPhone: {
    type: DataTypes.STRING,
    allowNull: false
  },
  bloodGroup: {
    type: DataTypes.STRING(10),
    allowNull: false
  },
  unitsNeeded: {
    type: DataTypes.INTEGER,
    defaultValue: 1
  },
  hospitalName: {
    type: DataTypes.STRING,
    allowNull: false
  },
  location: {
    type: DataTypes.STRING,
    allowNull: false
  },
  latitude: {
    type: DataTypes.DOUBLE,
    defaultValue: 23.8786
  },
  longitude: {
    type: DataTypes.DOUBLE,
    defaultValue: 90.3766
  },
  neededBefore: {
    type: DataTypes.STRING,
    allowNull: true
  },
  note: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  status: {
    type: DataTypes.ENUM('PENDING', 'ACTIVE', 'ACCEPTED', 'COMPLETED', 'CANCELLED'),
    defaultValue: 'PENDING'
  },
  acceptedDonorName: {
    type: DataTypes.STRING,
    allowNull: true
  },
  acceptedDonorPhone: {
    type: DataTypes.STRING,
    allowNull: true
  },
  selectedDonorCount: {
    type: DataTypes.INTEGER,
    defaultValue: 0
  }
}, {
  tableName: 'blood_requests',
  timestamps: true
});

module.exports = BloodRequest;
