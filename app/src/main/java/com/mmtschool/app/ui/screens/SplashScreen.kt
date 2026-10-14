package com.mmtschool.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mmtschool.app.R
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.navigation.Routes

@Composable
fun SplashScreen(
    navController: NavHostController,
    prefsRepository: UserPrefsRepository
) {
    LaunchedEffect(Unit) {
        val saved = prefsRepository.getConfirmedSessionOnce()
        val destination = if (saved != null && !saved.finalUrl.isNullOrBlank()) {
            Routes.webview(
                schoolDocId = saved.schoolDocId,
                schoolName = saved.schoolName,
                url = saved.finalUrl,
                isFirstLogin = false
            )
        } else {
            Routes.SCHOOL_LIST
        }
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icons),
            contentDescription = "App logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
    }
}