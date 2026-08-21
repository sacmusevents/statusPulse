import { Injectable } from '@angular/core';
import { initializeApp, FirebaseApp } from 'firebase/app';
import { getDatabase, Database, ref, get, onValue, Unsubscribe } from 'firebase/database';
import { environment } from '../environments/environment';
import { Session, SignalColor } from '../models/session.model';

/**
 * Angular Service: FirebaseService
 * --------------------------------
 * Connects directly to Firebase Realtime Database SDK.
 */
@Injectable({
  providedIn: 'root'
})
export class FirebaseService {
  private app: FirebaseApp;
  private db!: Database;
  private isInitialized = false;

  constructor() {
    // Initialize Firebase App SDK
    this.app = initializeApp(environment.firebase);
    this.initDatabase();
  }

  private initDatabase(): void {
    try {
      const url = environment.firebase.databaseURL;
      if (url && !url.includes('YOUR_PROJECT_ID')) {
        this.db = getDatabase(this.app, url);
      } else {
        this.db = getDatabase(this.app);
      }
      this.isInitialized = true;
    } catch (err) {
      console.warn('Failed to initialize Firebase Realtime Database with specified databaseURL:', err);
      try {
        this.db = getDatabase(this.app);
        this.isInitialized = true;
      } catch (fallbackErr) {
        console.error('Firebase Database initialization failed completely:', fallbackErr);
      }
    }
  }

  /**
   * Fetches active sessions directly from Firebase Realtime Database ('sessions' node).
   */
  async getActiveSessions(): Promise<Session[]> {
    console.log('Fetching active sessions from Firebase Realtime Database at:', environment.firebase.databaseURL);
    
    if (!this.isInitialized || !this.db) {
      console.warn('Firebase Database is not yet initialized.');
      return [];
    }

    try {
      const sessionsRef = ref(this.db, 'sessions');
      const snapshot = await get(sessionsRef);
      
      if (!snapshot.exists()) {
        console.warn('No active sessions found in Firebase Realtime Database at /sessions');
        return [];
      }

      const rawData = snapshot.val();
      
      // Parse sessions whether stored as an array or a key-value object
      const sessionsList: Session[] = Array.isArray(rawData)
        ? rawData
        : Object.keys(rawData).map(key => ({
            id: rawData[key].id || key,
            title: rawData[key].title || `Session ${key}`,
            status: rawData[key].status || 'active',
            lastUpdated: rawData[key].lastUpdated || new Date().toISOString()
          }));

      return sessionsList.filter(s => s.status === 'active');
    } catch (error) {
      console.error('Failed to fetch from Firebase Realtime Database. Please ensure Realtime Database is created in Firebase Console:', error);
      return [];
    }
  }

  /**
   * Subscribes to real-time signal changes for a specific session at 'signals/{sessionId}'.
   */
  subscribeToSignal(sessionId: string, callback: (color: SignalColor) => void): () => void {
    console.log(`Subscribing to Firebase Realtime Database signal updates for session: ${sessionId}`);

    if (!this.isInitialized || !this.db) {
      callback('green');
      return () => {};
    }

    try {
      const signalRef = ref(this.db, `signals/${sessionId}`);

      const unsubscribe: Unsubscribe = onValue(
        signalRef,
        (snapshot) => {
          if (snapshot.exists()) {
            const val = snapshot.val();
            // Support both string color value ("green", "yellow", "red") or object { color: "green" }
            const color: SignalColor = typeof val === 'string' ? (val as SignalColor) : (val?.color || 'green');
            console.log(`Realtime signal update received for ${sessionId}:`, color);
            callback(color);
          } else {
            console.log(`No signal data found at /signals/${sessionId}, defaulting to green`);
            callback('green');
          }
        },
        (error) => {
          console.error(`Firebase realtime listener error for session ${sessionId}:`, error);
        }
      );

      return () => {
        console.log(`Unsubscribing from Firebase signal updates for session: ${sessionId}`);
        unsubscribe();
      };
    } catch (err) {
      console.error('Failed to subscribe to Firebase Realtime Database signal updates:', err);
      callback('green');
      return () => {};
    }
  }
}
