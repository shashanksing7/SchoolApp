package com.mmtschool.app.utility

import androidx.navigation.NavHostController
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.navigation.Routes

suspend fun logout(
    prefsRepository: UserPrefsRepository,
    navController: NavHostController
) {
    val session = prefsRepository.getSessionOnce()

    prefsRepository.clearSession()
    navController.navigate(Routes.SCHOOL_LIST) {
        popUpTo(0) { inclusive = true }
    }

    if (!session?.rollNumber.isNullOrBlank()) {
        try {
            AttendanceNotificationManager().onReset(
                schoolId = session!!.schoolId.toString(),
                username = session.rollNumber!!
            )
        } catch (e: Exception) {
            // Network was down / call failed — don't lose the cleanup,
            // retry it opportunistically on the next successful page load.
            prefsRepository.savePendingReset(session!!.schoolId.toString(), session.rollNumber!!)
        }
    }
}

/**
 * Called from WebViewScreen whenever a page finishes loading successfully —
 * that's proof the network is currently up, so it's the cheapest reliable
 * moment to retry a reset that failed at logout time for a *previous* user.
 */
suspend fun flushPendingReset(prefsRepository: UserPrefsRepository) {
    val pending = prefsRepository.getPendingResetOnce() ?: return
    try {
        AttendanceNotificationManager().onReset(pending.schoolId, pending.username)
        prefsRepository.clearPendingReset()
    } catch (e: Exception) {
        // Still failing — leave it queued, try again on the next page load.
    }
}