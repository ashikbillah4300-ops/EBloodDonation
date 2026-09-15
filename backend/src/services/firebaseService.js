let admin = null;
try {
  const mod = require('firebase-admin');
  admin = mod && mod.default ? mod.default : mod;
} catch (e) {
  console.warn('[FirebaseService] firebase-admin package load notice:', e.message);
}

let firebaseInitialized = false;

function getApps() {
  if (!admin) return [];
  if (Array.isArray(admin.apps)) return admin.apps;
  if (admin.default && Array.isArray(admin.default.apps)) return admin.default.apps;
  return [];
}

// Initialize Firebase Admin SDK safely without crashing on boot
function initFirebaseAdmin() {
  if (firebaseInitialized) return;

  const existingApps = getApps();
  if (existingApps.length > 0) {
    firebaseInitialized = true;
    return;
  }

  if (!admin) {
    console.log('[FirebaseService] Firebase Admin SDK is not available. Operating in simulation mode.');
    return;
  }

  try {
    const projectId = process.env.FIREBASE_PROJECT_ID || 'eblooddonation-6af67';
    const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
    let privateKey = process.env.FIREBASE_PRIVATE_KEY;

    if (clientEmail && privateKey) {
      // Fix escaped newline characters if set in environment strings
      privateKey = privateKey.replace(/\\n/g, '\n');

      admin.initializeApp({
        credential: admin.credential.cert({
          projectId,
          clientEmail,
          privateKey
        })
      });
      firebaseInitialized = true;
      console.log('Firebase Admin SDK successfully initialized.');
    } else {
      console.log('Firebase Admin credentials not set in env (FIREBASE_CLIENT_EMAIL / FIREBASE_PRIVATE_KEY). Running with FCM mock/simulation mode.');
    }
  } catch (error) {
    console.warn('Firebase Admin initialization notice:', error.message);
  }
}

// Safe initialization
try {
  initFirebaseAdmin();
} catch (err) {
  console.warn('Firebase Admin initialization deferred:', err.message);
}

// Return compatible donor blood groups for a given recipient blood group
function getCompatibleDonorGroups(recipientGroup) {
  const normalized = (recipientGroup || '').trim().toUpperCase();
  switch (normalized) {
    case 'A+':
      return ['A+', 'A-', 'O+', 'O-'];
    case 'A-':
      return ['A-', 'O-'];
    case 'B+':
      return ['B+', 'B-', 'O+', 'O-'];
    case 'B-':
      return ['B-', 'O-'];
    case 'AB+':
      return ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
    case 'AB-':
      return ['AB-', 'A-', 'B-', 'O-'];
    case 'O+':
      return ['O+', 'O-'];
    case 'O-':
      return ['O-'];
    default:
      return [normalized];
  }
}

// In-memory deduplication cache: requestId -> timestamp
const notificationHistory = new Map();

/**
 * Send targeted FCM emergency blood alert notifications to eligible donors
 */
async function sendEmergencyBloodAlert(bloodRequest, matchingTokens) {
  if (!matchingTokens || matchingTokens.length === 0) {
    return { success: true, sentCount: 0, reason: 'No eligible donor tokens available' };
  }

  // Deduplication check: prevent spamming same request within 2 minutes
  const lastSent = notificationHistory.get(bloodRequest.id);
  const now = Date.now();
  if (lastSent && now - lastSent < 120000) {
    return { success: true, sentCount: 0, reason: 'Rate-limited: Alert already broadcast recently' };
  }
  notificationHistory.set(bloodRequest.id, now);

  // Clean old deduplication history
  if (notificationHistory.size > 500) {
    const cutoff = now - 600000;
    for (const [id, ts] of notificationHistory.entries()) {
      if (ts < cutoff) notificationHistory.delete(id);
    }
  }

  // Remove duplicates and empty tokens
  const uniqueTokens = Array.from(new Set(matchingTokens.filter(t => typeof t === 'string' && t.trim().length > 10)));
  if (uniqueTokens.length === 0) {
    return { success: true, sentCount: 0, reason: 'No valid unique FCM tokens' };
  }

  const apps = getApps();
  if (!firebaseInitialized || apps.length === 0 || !admin) {
    console.log(`[FCM Mock Alert] Urgent ${bloodRequest.bloodGroup} needed at ${bloodRequest.hospitalName}. Recipient tokens: ${uniqueTokens.length}`);
    return { success: true, sentCount: uniqueTokens.length, simulated: true };
  }

  try {
    const payload = {
      notification: {
        title: `🚨 জরুরি ${bloodRequest.bloodGroup} রক্তের প্রয়োজন!`,
        body: `${bloodRequest.hospitalName}, ${bloodRequest.location}-এ জরুরি রক্তদাতা প্রয়োজন। জীবন বাঁচাতে এগিয়ে আসুন।`
      },
      data: {
        type: 'EMERGENCY_BLOOD_REQUEST',
        requestId: String(bloodRequest.id),
        bloodGroup: String(bloodRequest.bloodGroup),
        hospitalName: String(bloodRequest.hospitalName || ''),
        location: String(bloodRequest.location || ''),
        unitsNeeded: String(bloodRequest.unitsNeeded || '1'),
        requesterPhone: String(bloodRequest.requesterPhone || ''),
        requesterName: String(bloodRequest.requesterName || ''),
        soundEnabled: 'true',
        vibrateEnabled: 'true',
        priority: 'high'
      }
    };

    // Send in batches of 500 using multicast
    let successCount = 0;
    const batchSize = 500;
    for (let i = 0; i < uniqueTokens.length; i += batchSize) {
      const batchTokens = uniqueTokens.slice(i, i + batchSize);
      const response = await admin.messaging().sendEachForMulticast({
        tokens: batchTokens,
        ...payload,
        android: {
          priority: 'high',
          notification: {
            channelId: 'emergency_blood_channel',
            sound: 'default',
            priority: 'high'
          }
        }
      });
      successCount += response.successCount;
    }

    return { success: true, sentCount: successCount, totalEligible: uniqueTokens.length };
  } catch (error) {
    console.error('Error sending FCM alert:', error);
    return { success: false, error: error.message };
  }
}

/**
 * Verify Firebase ID Token for authenticated requests
 */
async function verifyFirebaseIdToken(idToken) {
  const apps = getApps();
  if (!firebaseInitialized || apps.length === 0 || !admin) {
    return null;
  }
  try {
    return await admin.auth().verifyIdToken(idToken);
  } catch (error) {
    console.error('Firebase ID token verification failed:', error.message);
    return null;
  }
}

module.exports = {
  getCompatibleDonorGroups,
  sendEmergencyBloodAlert,
  verifyFirebaseIdToken
};
