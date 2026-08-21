// Firebase Configuration for StatusPulse (Project ID: statuspulse-dd480)
// To complete setup, get your Web API key from Firebase Console -> Project Settings -> General -> Web Apps

export const environment = {
  production: false,
  firebase: {
    apiKey: "YOUR_WEB_API_KEY", // Get Web API Key from Firebase Console -> Project Settings
    authDomain: "statuspulse-dd480.firebaseapp.com",
    databaseURL: "https://statuspulse-dd480-default-rtdb.firebaseio.com",
    projectId: "statuspulse-dd480",
    storageBucket: "statuspulse-dd480.firebasestorage.app",
    messagingSenderId: "105646632378055265888",
    appId: "1:105646632378055265888:web:statuspulse"
  }
};
