package com.signalapp

import android.content.Context

object SupabaseManager {
    suspend fun getSessions(context: Context): List<Session> {
        return FirebaseManager.getSessions(context)
    }

    suspend fun insertSession(session: Session, context: Context) {
        FirebaseManager.insertSession(session, context)
    }

    suspend fun insertSignal(signal: Signal, context: Context) {
        FirebaseManager.insertSignal(signal, context)
    }

    suspend fun updateSignal(sessionId: String, color: String, timestamp: String, context: Context) {
        FirebaseManager.updateSignal(sessionId, color, timestamp, context)
    }

    suspend fun deleteSession(sessionId: String, context: Context) {
        FirebaseManager.deleteSession(sessionId, context)
    }
}
