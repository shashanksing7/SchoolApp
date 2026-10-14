package com.mmtschool.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Generic holder for whatever fields live in the "support" collection
 * (e.g. email, phone, message / FAQ text). Reading it generically means
 * this keeps working even if the fields in Firestore change later.
 */
data class SupportInfo(
    val email: String? = null,
    val phone: String? = null,
    val message: String? = null,
    val website: String? = null
)

class SupportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getSupportInfo(): Result<SupportInfo> {
        return try {
            val snapshot = db.collection("support").limit(1).get().await()
            val doc = snapshot.documents.firstOrNull()
            val data = doc?.data ?: emptyMap<String, Any>()
            Result.Success(
                SupportInfo(
                    email = data["email"] as? String,
                    phone = data["phone"] as? String,
                    message = data["message"] as? String,
                    website = data["website"] as? String
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load support info")
        }
    }
}
