package com.mmtschool.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mmtschool.app.data.UserPrefsRepository
import com.mmtschool.app.ui.screens.*

@Composable
fun AppNavigation(prefsRepository: UserPrefsRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(navController, prefsRepository)
        }

        composable(Routes.SCHOOL_LIST) {
            SchoolListScreen(navController)
        }

        composable(
            route = Routes.ROLE_SELECT,
            arguments = listOf(navArgument("schoolDocId") { type = NavType.StringType })
        ) { backStackEntry ->
            val schoolDocId = Routes.decode(
                backStackEntry.arguments?.getString("schoolDocId").orEmpty()
            )
            RoleSelectionScreen(navController, schoolDocId)
        }

        composable(
            route = Routes.ROLL_NUMBER,
            arguments = listOf(
                navArgument("schoolDocId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val schoolDocId = Routes.decode(
                backStackEntry.arguments?.getString("schoolDocId").orEmpty()
            )
            val role = Routes.decode(
                backStackEntry.arguments?.getString("role").orEmpty()
            )
            RollNumberScreen(navController, schoolDocId, role)
        }

        composable(
            route = Routes.WEBVIEW,
            arguments = listOf(
                navArgument("schoolDocId") { type = NavType.StringType },
                navArgument("schoolName") { type = NavType.StringType },
                navArgument("encodedUrl") { type = NavType.StringType },
                navArgument("isFirstLogin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val schoolDocId = Routes.decode(
                backStackEntry.arguments?.getString("schoolDocId").orEmpty()
            )
            val schoolName = Routes.decode(
                backStackEntry.arguments?.getString("schoolName").orEmpty()
            )
            val url = Routes.decode(
                backStackEntry.arguments?.getString("encodedUrl").orEmpty()
            )
            val isFirstLogin = backStackEntry.arguments?.getBoolean("isFirstLogin") ?: false

            WebViewScreen(
                navController = navController,
                schoolDocId = schoolDocId,
                schoolName = schoolName,
                url = url,
                isFirstLogin = isFirstLogin
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }
    }
}
