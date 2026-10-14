package com.mmtschool.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mmtschool.app.data.SessionData
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.ui.AppBottomBar
import com.mmtschool.app.ui.AppTopBar
import com.mmtschool.app.ui.NavTab
import com.mmtschool.app.utility.logout
import kotlinx.coroutines.launch

/** No Firestore "support" collection anymore — all copy is hardcoded. */
private object AboutContent {
    const val EMAIL = "info@marsmetatech.com"
    const val PHONE = "+91 9318429812"
    const val WHATSAPP = "919318429812"
    const val WEBSITE = "https://mmtschool.com"
    const val CTA_TITLE = "About MMT School"
    const val CTA_DESCRIPTION =
        "MMT School is built by MarsMeta Tech to give students, parents and staff " +
                "a fast, simple way to reach their school's ERP portal from their phone."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefsRepository = remember { UserPrefsRepository(context) }
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf<SessionData?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        session = prefsRepository.getConfirmedSessionOnce()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                showBack = false,
                onBack = { navController.popBackStack() },
                showLogout = true,
                onLogout = { showLogoutConfirm = true }
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = NavTab.SETTINGS,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.HOME -> navController.popBackStack()
                        NavTab.SETTINGS -> { /* already here */ }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                val logoUrl = session?.logoURL
                if (!logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = session?.schoolName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                session?.schoolName ?: "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Manage your account and support",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            SettingsSectionLabel("Support")
            SettingsGroup {
                SettingsRow(icon = Icons.Default.Email, title = "Email us", subtitle = AboutContent.EMAIL) {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${AboutContent.EMAIL}")))
                }
                SettingsDivider()
                SettingsRow(icon = Icons.Default.Call, title = "Call support", subtitle = AboutContent.PHONE) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${AboutContent.PHONE}")))
                }
                SettingsDivider()
                SettingsRow(icon = Icons.Default.Chat, title = "WhatsApp", subtitle = "Message us directly") {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${AboutContent.WHATSAPP}")))
                }
            }

            Spacer(Modifier.height(20.dp))

            SettingsSectionLabel("About")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = "Visit our website",
                    subtitle = AboutContent.WEBSITE.removePrefix("https://")
                ) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AboutContent.WEBSITE)))
                }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = AboutContent.CTA_TITLE,
                    subtitle = "A MarsMeta Tech company"
                ) {
                    showAbout = true
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "MMT School by MarsMeta Tech",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(AboutContent.CTA_TITLE) },
            text = { Text(AboutContent.CTA_DESCRIPTION) },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close") } }
        )
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
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    Divider(modifier = Modifier.padding(start = 60.dp), thickness = 0.6.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}