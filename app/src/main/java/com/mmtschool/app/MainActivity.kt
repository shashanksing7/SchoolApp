package com.mmtschool.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.navigation.AppNavigation
import com.mmtschool.app.ui.theme.SchoolAppTheme
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mmtschool.app.utility.AttendanceNotificationManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsRepository = UserPrefsRepository(applicationContext)
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            SchoolAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RequestNotificationPermission()
                    AppNavigation(prefsRepository = prefsRepository)
                }
            }
        }
    }
}

@Composable
fun RequestNotificationPermission(
    notificationManager: AttendanceNotificationManager = AttendanceNotificationManager()
) {
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { notificationManager.subscribeAllUsers() }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Below API 33, notifications are granted by default — no runtime prompt needed.
            notificationManager.subscribeAllUsers()
        }
    }
}