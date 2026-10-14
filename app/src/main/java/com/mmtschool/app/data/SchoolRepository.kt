package com.mmtschool.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
class SchoolRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getSchools(): Result<List<School>> {
        return try {
            val snapshot = db.collection("schools").get().await()
            val schools = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                School(
                    docId = doc.id,
                    id = (data["id"] as? Long)?.toInt() ?: 0,
                    schoolName = data["schoolName"] as? String ?: doc.id,
                    logoURL = data["logoURL"] as? String ?: "",
                    priority = (data["priority"] as? Long)?.toInt() ?: Int.MAX_VALUE,
                    staffURL = data["staffURL"] as? String ?: "",
                    studentURL = data["studentURL"] as? String ?: "",
                    adminURL = data["adminURL"] as? String ?: "",
                    status = data["status"] as? String ?: "hide",
                    type = data["type"] as? String ?: ""
                )
            }
                .filter { it.isVisible }
                .sortedBy { it.priority }

            Result.Success(schools)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load schools")
        }
    }

    suspend fun getSchoolById(docId: String): Result<School?> {
        return try {
            val doc = db.collection("schools").document(docId).get().await()
            val data = doc.data
            if (data == null) {
                Result.Success(null)
            } else {
                Result.Success(
                    School(
                        docId = doc.id,
                        id = (data["id"] as? Long)?.toInt() ?: 0,
                        schoolName = data["schoolName"] as? String ?: doc.id,
                        logoURL = data["logoURL"] as? String ?: "",
                        priority = (data["priority"] as? Long)?.toInt() ?: Int.MAX_VALUE,
                        staffURL = data["staffURL"] as? String ?: "",
                        studentURL = data["studentURL"] as? String ?: "",
                        adminURL = data["adminURL"] as? String ?: "",
                        status = data["status"] as? String ?: "hide",
                        type = data["type"] as? String ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load school")
        }
    }
}