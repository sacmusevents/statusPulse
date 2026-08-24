// Dynamic Firebase Environment Config
// Loads configuration dynamically from local service account JSON if present

declare const require: any;

let localServiceAccount: any = null;

try {
  // Dynamically import local service account key file (ignored by Git)
  localServiceAccount = require('../../../../functions/statuspulse-dd480-firebase-adminsdk-fbsvc-3f769543e4.json');
} catch (e) {
  // Fallback if local key file is not present
  localServiceAccount = null;
}

const projectId = localServiceAccount?.project_id || "statuspulse-dd480";

export const environment = {
  production: false,
  firebase: {
    apiKey: localServiceAccount?.private_key_id || "local-dev-key",
    authDomain: `${projectId}.firebaseapp.com`,
    databaseURL: `https://${projectId}-default-rtdb.firebaseio.com`,
    projectId: projectId,
    storageBucket: `${projectId}.firebasestorage.app`,
    messagingSenderId: localServiceAccount?.client_id || "105646632378055265888",
    appId: `1:${localServiceAccount?.client_id || "105646632378055265888"}:web:statuspulse`
  }
};
