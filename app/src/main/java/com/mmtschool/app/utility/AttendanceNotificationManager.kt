package com.mmtschool.app.utility



import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Owns FCM topic subscriptions and the attendance_tokens Firestore records.
 * See dev-doc summary table:
 *   Permission granted -> subscribe all_users
 *   Profile set         -> subscribe school_<schoolId>, write token record
 *   Reset                -> unsubscribe school_<schoolId>, delete token record
 *   Token refresh        -> overwrite token on existing record
 */


class AttendanceNotificationManager(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val TOPIC_ALL_USERS = "all_users"
        private const val TOKENS_COLLECTION = "attendance_tokens"

        private fun schoolTopic(schoolId: String) = "school_$schoolId"
        private fun tokenDocId(schoolId: String, username: String) = "${schoolId}_$username"
    }

    suspend fun subscribeAllUsers() {
        messaging.subscribeToTopic(TOPIC_ALL_USERS).await()
    }

    suspend fun onProfileSet(schoolId: String, username: String) {
        messaging.subscribeToTopic(schoolTopic(schoolId)).await()
        val token = messaging.token.await()
        writeToken(schoolId, username, token)
    }

    suspend fun onReset(schoolId: String, username: String) {
        messaging.unsubscribeFromTopic(schoolTopic(schoolId)).await()
        firestore.collection(TOKENS_COLLECTION)
            .document(tokenDocId(schoolId, username))
            .delete()
            .await()
    }

    suspend fun refreshToken(schoolId: String, username: String, newToken: String) {
        writeToken(schoolId, username, newToken)
    }

    private suspend fun writeToken(schoolId: String, username: String, token: String) {
        val data = mapOf(
            "fcmToken" to token,
            "userId" to username,
            "schoolId" to schoolId
        )
        firestore.collection(TOKENS_COLLECTION)
            .document(tokenDocId(schoolId, username))
            .set(data)
            .await()
    }
}