package com.mmtschool.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mmtschool.app.data.Result
import com.mmtschool.app.data.School
import com.mmtschool.app.data.SchoolRepository
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.data.UserRole
import com.mmtschool.app.navigation.Routes
import com.mmtschool.app.ui.AppTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    navController: NavHostController,
    schoolDocId: String,
    repository: SchoolRepository = SchoolRepository()
) {
    val context = LocalContext.current
    val prefsRepository = remember { UserPrefsRepository(context) }

    var school by remember { mutableStateOf<School?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(schoolDocId) {
        val session = prefsRepository.getSessionOnce()
        if (session != null && session.schoolDocId == schoolDocId) {
            school = School(
                docId = session.schoolDocId,
                schoolName = session.schoolName,
                logoURL = session.logoURL,
                staffURL = session.staffURL,
                studentURL = session.studentURL,
                adminURL = session.adminURL,
                type = session.type
            )
            isLoading = false
        } else {
            when (val result = repository.getSchoolById(schoolDocId)) {
                is Result.Success -> {
                    school = result.data
                    result.data?.let { prefsRepository.savePendingSchool(it) }
                    isLoading = false
                }
                is Result.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "",
                showBack = navController.previousBackStackEntry != null,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text("Error: $errorMessage")
                school == null -> Text("School not found")
                else -> {
                    val currentSchool = school!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentSchool.logoURL.isNotBlank()) {
                                AsyncImage(
                                    model = currentSchool.logoURL,
                                    contentDescription = currentSchool.schoolName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            currentSchool.schoolName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Choose how you'd like to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(32.dp))

                        // Both roles now go through RollNumberScreen — student always
                        // enters roll/admission number (erp-pro and elite-lite alike),
                        // staff enters staff ID or uses the bypass link on that screen.
                        RoleCard(
                            title = UserRole.STUDENT_PARENT.label,
                            subtitle = "Access grades, attendance & more",
                            icon = Icons.Default.Group
                        ) {
                            navController.navigate(
                                Routes.rollNumber(schoolDocId, UserRole.STUDENT_PARENT.name)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        RoleCard(
                            title = UserRole.STAFF.label,
                            subtitle = "Manage classes & school records",
                            icon = Icons.Default.Badge
                        ) {
                            navController.navigate(
                                Routes.rollNumber(schoolDocId, UserRole.STAFF.name)
                            )
                        }

                        // Pushes the admin link to the bottom of the screen instead of
                        // stacking it directly under the two role cards.
                        Spacer(Modifier.weight(1f))

                        Text(
                            "Open admin portal instead",
                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val adminUrl = currentSchool.adminURL
                                if (adminUrl.isNullOrBlank()) {
                                    Toast.makeText(context, "Feature not available", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }

                                scope.launch {
                                    navController.navigate(
                                        Routes.webview(
                                            schoolDocId = currentSchool.docId,
                                            schoolName = currentSchool.schoolName,
                                            url = adminUrl,
                                            isFirstLogin = true
                                        )
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}