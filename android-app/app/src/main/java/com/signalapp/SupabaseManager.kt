package com.signalapp

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
    val status: String? = "active"
)

@Serializable
data class Signal(
    val id: String,
    val session_id: String,
    val color: String,
    val updated_at: String? = null
)

object SupabaseManager {
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val json = Json { ignoreUnknownKeys = true }

    fun buildRequest(method: String, table: String, body: String? = null, query: String? = null): Request {
        val url = if (query != null) {
            "$SUPABASE_URL/rest/v1/$table?$query"
        } else {
            "$SUPABASE_URL/rest/v1/$table"
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer $SUPABASE_KEY")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=minimal")

        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> {
                requestBuilder.post(body?.toRequestBody("application/json".toMediaType()) ?: "{}".toRequestBody("application/json".toMediaType()))
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

    suspend fun getSessions(): List<Session> {
        return try {
            val request = buildRequest("GET", "sessions", null, "status=eq.active")
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: "[]"
            if (response.isSuccessful) {
                json.decodeFromString(body)
            } else {
                android.util.Log.e("SupabaseManager", "getSessions error: ${response.code} - $body")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "getSessions exception", e)
            emptyList()
        }
    }

    suspend fun insertSession(session: Session) {
        try {
            val body = json.encodeToString(session)
            val request = buildRequest("POST", "sessions", body)
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("SupabaseManager", "insertSession error: ${response.code} - ${response.body?.string()}")
                throw Exception("insertSession failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "insertSession exception", e)
            throw e
        }
    }

    suspend fun insertSignal(signal: Signal) {
        try {
            val body = json.encodeToString(signal)
            val request = buildRequest("POST", "signals", body)
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("SupabaseManager", "insertSignal error: ${response.code} - ${response.body?.string()}")
                throw Exception("insertSignal failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "insertSignal exception", e)
            throw e
        }
    }

    suspend fun updateSignal(sessionId: String, color: String, timestamp: String) {
        try {
            @Serializable
            data class SignalUpdate(val color: String, val updated_at: String)

            val body = json.encodeToString(SignalUpdate(color, timestamp))
            val request = buildRequest("PATCH", "signals", body, "session_id=eq.$sessionId")
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("SupabaseManager", "updateSignal error: ${response.code} - ${response.body?.string()}")
                throw Exception("updateSignal failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "updateSignal exception", e)
            throw e
        }
    }

    suspend fun deleteSession(sessionId: String) {
        try {
            val request = buildRequest("DELETE", "sessions", null, "id=eq.$sessionId")
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("SupabaseManager", "deleteSession error: ${response.code} - ${response.body?.string()}")
                throw Exception("deleteSession failed: ${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseManager", "deleteSession exception", e)
            throw e
        }
    }
}
