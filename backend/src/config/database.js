const { Sequelize } = require('sequelize');
require('dotenv').config();

let connectionString = process.env.DATABASE_URL;
if (connectionString) {
  // Strip channel_binding which is not supported by node-postgres
  connectionString = connectionString.replace(/[?&]channel_binding=[^&]+/g, '');
  if (!connectionString.includes('?') && connectionString.includes('&')) {
    connectionString = connectionString.replace('&', '?');
  }
}

const isSslRequired = connectionString && (
  connectionString.includes('sslmode=require') ||
  connectionString.includes('neon.tech') ||
  process.env.NODE_ENV === 'production'
);

const sequelize = connectionString
  ? new Sequelize(connectionString, {
      dialect: 'postgres',
      logging: process.env.NODE_ENV === 'development' ? console.log : false,
      dialectOptions: isSslRequired ? {
        ssl: {
          require: true,
          rejectUnauthorized: false
        }
      } : {}
    })
  : new Sequelize(
      process.env.DB_NAME || 'eblood_db',
      process.env.DB_USER || 'postgres',
      process.env.DB_PASSWORD || 'postgres',
      {
        host: process.env.DB_HOST || 'localhost',
        port: process.env.DB_PORT || 5432,
        dialect: 'postgres',
        logging: false
      }
    );

module.exports = { sequelize };
