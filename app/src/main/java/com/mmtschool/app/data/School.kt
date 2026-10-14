package com.mmtschool.app.data

/**
 * Mirrors a document in the Firestore "schools" collection, e.g.:
 * {
 *   id: 3,
 *   logoURL: "https://res.cloudinary.com/.../logo.png",
 *   priority: 1,
 *   schoolName: "SR DAV School",
 *   staffURL: "https://srdav.mmtschool.com/site/login",
 *   status: "show",
 *   studentURL: "https://srdav.mmtschool.com/site/userlogin",
 *   type: "erp-pro"
 * }
 */


/**
 * Mirrors a document in the Firestore "schools" collection.
 */
data class School(
    val docId: String = "",
    val id: Int = 0,
    val schoolName: String = "",
    val logoURL: String = "",
    val priority: Int = Int.MAX_VALUE,
    val staffURL: String = "",
    val studentURL: String = "",
    val adminURL: String = "",
    val status: String = "hide",
    val type: String = ""
) {
    val isVisible: Boolean
        get() = status.equals("show", ignoreCase = true)
}



enum class UserRole(val label: String) {
    STUDENT_PARENT("Student / Parent"),
    STAFF("Staff")
}