package com.mmtschool.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject


/**
 * Everything the app knows about the current login attempt / session.
 * Written in stages:
 *  1. School picked          -> schoolDocId, schoolName, logoURL, staffURL, studentURL, type set. isConfirmed = false.
 *  2. Roll number submitted  -> role, rollNumber, finalUrl added. Still isConfirmed = false.
 *  3a. WebView load succeeds -> isConfirmed = true. Session is kept permanently.
 *  3b. WebView load fails    -> entire session is wiped (back to school picker).
 */

private val Context.dataStore by preferencesDataStore(name = "user_prefs")


data class SessionData(
    val schoolDocId: String,
    val schoolId: Int,
    val schoolName: String,
    val logoURL: String,
    val staffURL: String,
    val studentURL: String,
    val adminURL: String,
    val type: String,
    val role: String? = null,
    val rollNumber: String? = null,
    val finalUrl: String? = null,
    val isConfirmed: Boolean = false
)

/**
 * An FCM/Firestore reset (see AttendanceNotificationManager.onReset) that
 * failed at logout time, usually due to no network. Held here so it can be
 * retried the next time any WebView page load succeeds — that's proof the
 * network is up. Only one is tracked at a time; see flushPendingReset.
 */
data class PendingReset(
    val schoolId: String,
    val username: String
)

class UserPrefsRepository(private val context: Context) {

    private object Keys {
        val SESSION_JSON = stringPreferencesKey("session_json")
        val PENDING_RESET_JSON = stringPreferencesKey("pending_reset_json")
    }

    val sessionFlow: Flow<SessionData?> = context.dataStore.data.map { prefs ->
        prefs[Keys.SESSION_JSON]?.let { decode(it) }
    }

    suspend fun getSessionOnce(): SessionData? = sessionFlow.first()

    suspend fun getConfirmedSessionOnce(): SessionData? =
        getSessionOnce()?.takeIf { it.isConfirmed }

    suspend fun savePendingSchool(school: School) {
        val session = SessionData(
            schoolDocId = school.docId,
            schoolId = school.id,
            schoolName = school.schoolName,
            logoURL = school.logoURL,
            staffURL = school.staffURL,
            studentURL = school.studentURL,
            adminURL = school.adminURL,
            type = school.type,
            isConfirmed = false
        )
        write(session)
    }

    suspend fun saveRoleAndRollNumber(role: String, rollNumber: String, finalUrl: String) {
        val current = getSessionOnce() ?: return
        write(current.copy(role = role, rollNumber = rollNumber, finalUrl = finalUrl))
    }

    suspend fun confirmSession() {
        val current = getSessionOnce() ?: return
        write(current.copy(isConfirmed = true))
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.remove(Keys.SESSION_JSON) }
    }

    suspend fun savePendingReset(schoolId: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PENDING_RESET_JSON] = JSONObject().apply {
                put("schoolId", schoolId)
                put("username", username)
            }.toString()
        }
    }

    suspend fun getPendingResetOnce(): PendingReset? {
        val json = context.dataStore.data.first()[Keys.PENDING_RESET_JSON] ?: return null
        return try {
            val o = JSONObject(json)
            PendingReset(o.getString("schoolId"), o.getString("username"))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearPendingReset() {
        context.dataStore.edit { it.remove(Keys.PENDING_RESET_JSON) }
    }

    private suspend fun write(session: SessionData) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SESSION_JSON] = encode(session)
        }
    }

    private fun encode(s: SessionData): String = JSONObject().apply {
        put("schoolDocId", s.schoolDocId)
        put("schoolId", s.schoolId)
        put("schoolName", s.schoolName)
        put("logoURL", s.logoURL)
        put("staffURL", s.staffURL)
        put("studentURL", s.studentURL)
        put("adminURL", s.adminURL)
        put("type", s.type)
        put("role", s.role ?: JSONObject.NULL)
        put("rollNumber", s.rollNumber ?: JSONObject.NULL)
        put("finalUrl", s.finalUrl ?: JSONObject.NULL)
        put("isConfirmed", s.isConfirmed)
    }.toString()

    private fun decode(json: String): SessionData? = try {
        val o = JSONObject(json)
        SessionData(
            schoolDocId = o.getString("schoolDocId"),
            schoolId = o.optInt("schoolId", 0),
            schoolName = o.getString("schoolName"),
            logoURL = o.optString("logoURL", ""),
            staffURL = o.optString("staffURL", ""),
            studentURL = o.optString("studentURL", ""),
            adminURL = o.optString("adminURL", ""),
            type = o.optString("type", ""),
            role = if (o.isNull("role")) null else o.getString("role"),
            rollNumber = if (o.isNull("rollNumber")) null else o.getString("rollNumber"),
            finalUrl = if (o.isNull("finalUrl")) null else o.getString("finalUrl"),
            isConfirmed = o.optBoolean("isConfirmed", false)
        )
    } catch (e: Exception) {
        null
    }
}