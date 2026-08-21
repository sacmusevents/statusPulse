/**
 * TypeScript Data Models
 * Learning Point: In Angular & TypeScript, defining strict interfaces 
 * ensures type safety across components and services.
 */

export interface Session {
  id: string;
  title: string;
  status: 'active' | 'inactive';
  lastUpdated?: string; // ISO date string or relative timestamp
}

export type SignalColor = 'green' | 'yellow' | 'red';

export interface SignalState {
  sessionId: string;
  color: SignalColor;
  updatedAt: string;
}
