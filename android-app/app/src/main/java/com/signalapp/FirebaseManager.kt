package com.signalapp

import android.content.Context
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class Session(
    val id: String,
    val title: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val status: String? = "active",
    val lastUpdated: String? = null
)

@Serializable
data class Signal(
    val id: String,
    val session_id: String,
    val color: String,
    val updated_at: String? = null,
    val updatedAt: String? = null
)

object FirebaseManager {
    private val FIREBASE_DB_URL = BuildConfig.FIREBASE_DB_URL

    val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val json = Json { ignoreUnknownKeys = true }

    fun buildRequest(method: String, path: String, context: Context, body: String? = null): Request {
        val firebaseKey = KeyManager.getSupabaseKey(context)
        
        // Construct standard Firebase Realtime Database REST URL (path + .json)
        val cleanBaseUrl = FIREBASE_DB_URL.trimEnd('/')
        var url = "$cleanBaseUrl/$path.json"
        
        if (!firebaseKey.isNullOrEmpty()) {
            url += "?auth=$firebaseKey"
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")

        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> {
                requestBuilder.post(body?.toRequestBody("application/json".toMediaType()) ?: "{}".toRequestBody("application/json".toMediaType()))
            }
            "PUT" -> {
                requestBuilder.put(body?.toRequestBody("application/json".toMediaType()) ?: "{}".toRequestBody("application/json".toMediaType()))
            }
            "PATCH" -> {
                requestBuilder.patch(body?.toRequestBody("application/json".toMediaType()) ?: "{}".toRequestBody("application/json".toMediaType()))
            }
            "DELETE" -> {
                requestBuilder.delete()
            }
        }

        return requestBuilder.build()
    }

    suspend fun getSessions(context: Context): List<Session> {
        return try {
            val request = buildRequest("GET", "sessions", context)
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: "{}"
            if (response.isSuccessful) {
                if (body == "null" || body.trim() == "{}") return emptyList()

                val sessionsList = mutableListOf<Session>()
                try {
                    // Try parsing as JSON object map { "session-1": { ... } }
                    val map = json.decodeFromString<Map<String, Session>>(body)
                    map.forEach { (key, session) ->
                        val fixedSession = session.copy(
                            id = session.id.ifEmpty { key },
                            status = session.status ?: "active"
                        )
                        if (fixedSession.status == "active") {
                            sessionsList.add(fixedSession)
                        }
                    }
                } catch (e: Exception) {
                    // Try parsing as array
                    val list = json.decodeFromString<List<Session>>(body)
                    sessionsList.addAll(list.filter { it.status == "active" })
                }

                sessionsList
            } else {
                android.util.Log.e("FirebaseManager", "getSessions error: ${response.code} - $body")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "getSessions exception", e)
            emptyList()
        }
    }

    suspend fun insertSession(session: Session, context: Context) {
        try {
            val body = json.encodeToString(session)
            // Use PUT to store session directly at /sessions/{sessionId}.json
            val request = buildRequest("PUT", "sessions/${session.id}", context, body)
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("FirebaseManager", "insertSession error: ${response.code} - ${response.body?.string()}")
                throw Exception("insertSession failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "insertSession exception", e)
            throw e
        }
    }

    suspend fun insertSignal(signal: Signal, context: Context) {
        try {
            @Serializable
            data class SignalPayload(val color: String, val updatedAt: String, val session_id: String)

            val payload = SignalPayload(
                color = signal.color,
                updatedAt = signal.updated_at ?: signal.updatedAt ?: "",
                session_id = signal.session_id
            )
            val body = json.encodeToString(payload)
            // Store signal directly at /signals/{sessionId}.json
            val request = buildRequest("PUT", "signals/${signal.session_id}", context, body)
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("FirebaseManager", "insertSignal error: ${response.code} - ${response.body?.string()}")
                throw Exception("insertSignal failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "insertSignal exception", e)
            throw e
        }
    }

    suspend fun updateSignal(sessionId: String, color: String, timestamp: String, context: Context) {
        try {
            android.util.Log.d("FirebaseManager", "[SIGNAL_UPDATE] updateSignal: sessionId=$sessionId, color=$color, URL=$FIREBASE_DB_URL")
            @Serializable
            data class SignalUpdate(val color: String, val updatedAt: String)

            val body = json.encodeToString(SignalUpdate(color, timestamp))
            // PATCH request to /signals/{sessionId}.json
            val request = buildRequest("PATCH", "signals/$sessionId", context, body)
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                android.util.Log.e("FirebaseManager", "[SIGNAL_UPDATE] updateSignal error: ${response.code} - $errorBody")
                throw Exception("updateSignal failed: ${response.code}")
            }
            android.util.Log.d("FirebaseManager", "[SIGNAL_UPDATE] updateSignal successful")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "[SIGNAL_UPDATE] updateSignal exception: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteSession(sessionId: String, context: Context) {
        try {
            // Delete session and signal node
            val reqSession = buildRequest("DELETE", "sessions/$sessionId", context)
            httpClient.newCall(reqSession).execute()

            val reqSignal = buildRequest("DELETE", "signals/$sessionId", context)
            httpClient.newCall(reqSignal).execute()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "deleteSession exception", e)
            throw e
        }
    }
}
