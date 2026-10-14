package com.mmtschool.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.navigation.Routes
import com.mmtschool.app.ui.AppBottomBar
import com.mmtschool.app.ui.AppTopBar
import com.mmtschool.app.ui.NavTab
import com.mmtschool.app.utility.AttendanceNotificationManager
import com.mmtschool.app.utility.flushPendingReset
import com.mmtschool.app.utility.logout
import kotlinx.coroutines.launch

private enum class LoadState { LOADING, SUCCESS, ERROR }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    navController: NavHostController,
    schoolDocId: String,
    schoolName: String,
    url: String,
    isFirstLogin: Boolean
) {
    val context = LocalContext.current
    val prefsRepository = remember { UserPrefsRepository(context) }
    val scope = rememberCoroutineScope()

    var webViewState by rememberSaveable { mutableStateOf(Bundle()) }
    var loadState by remember {
        mutableStateOf(if (webViewState.isEmpty) LoadState.LOADING else LoadState.SUCCESS)
    }
    var hasSavedSession by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = schoolName,
                showBack = false,
                onBack = { navController.popBackStack() },
                showLogout = true,
                onLogout = { showLogoutConfirm = true }
            )
        },
        bottomBar = {
            if (loadState == LoadState.SUCCESS) {
                AppBottomBar(
                    selectedTab = NavTab.HOME,
                    onTabSelected = { tab ->
                        when (tab) {
                            NavTab.HOME -> {
                                val currentUrl = webViewRef?.url
                                if (currentUrl != url && currentUrl != "$url/") {
                                    webViewRef?.loadUrl(url)
                                }
                            }
                            NavTab.SETTINGS -> navController.navigate(Routes.SETTINGS)
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, u: String?, favicon: Bitmap?) {
                                loadState = LoadState.LOADING
                            }

                            override fun onPageFinished(view: WebView?, u: String?) {
                                if (loadState != LoadState.ERROR) {
                                    loadState = LoadState.SUCCESS

                                    // A successful load is proof the network is up right
                                    // now — cheapest reliable moment to retry a reset
                                    // that failed at a previous logout. No-op if none queued.
                                    scope.launch { flushPendingReset(prefsRepository) }

                                    if (isFirstLogin && !hasSavedSession) {
                                        hasSavedSession = true
                                        scope.launch {
                                            prefsRepository.confirmSession()
                                            val session = prefsRepository.getSessionOnce()
                                            if (!session?.rollNumber.isNullOrBlank()) {
                                                AttendanceNotificationManager().onProfileSet(
                                                    schoolId = session!!.schoolId.toString(),
                                                    username = session.rollNumber!!
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame != false) {
                                    loadState = LoadState.ERROR
                                    if (isFirstLogin) {
                                        scope.launch { prefsRepository.clearSession() }
                                    }
                                }
                            }
                        }
                        webViewRef = this

                        if (!webViewState.isEmpty) {
                            restoreState(webViewState)
                        } else {
                            loadUrl(url)
                        }
                    }
                },
                onRelease = { view ->
                    val bundle = Bundle()
                    view.saveState(bundle)
                    webViewState = bundle
                }
            )

            if (loadState == LoadState.LOADING) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (loadState == LoadState.ERROR) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Couldn't load the page. Check your connection and try again.", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            loadState = LoadState.LOADING
                            webViewRef?.loadUrl(url)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll be taken back to the school picker and asked to log in again.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    scope.launch { logout(prefsRepository, navController) }
                }) {
                    Text("Log out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}