package com.enach.client.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.enach.client.ui.screens.*
import com.enach.client.ui.screens.DemoDataSeparationScreen

@Composable
fun ENachNavHost(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        // Create Job Screen
        composable("create_job") {
            CreateJobScreen(navController = navController)
        }
        
        // Job List Screen
        composable(
            route = "job_list?status={status}",
            arguments = listOf(
                navArgument("status") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            JobListScreen(
                navController = navController,
                statusFilter = status
            )
        }
        
        // Job Details Screen
        composable(
            route = "job_details/{jobId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailsScreen(
                navController = navController,
                jobId = jobId
            )
        }
        
        // Validation Screen
        composable(
            route = "validate/{jobId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            ValidationScreen(
                navController = navController,
                jobId = jobId
            )
        }
        
        // Placeholder screens for other features
        composable("capture") {
            PlaceholderScreen(title = "Camera Capture")
        }
        
        composable("upload") {
            PlaceholderScreen(title = "Bulk Upload")
        }
        
        composable("reports") {
            PlaceholderScreen(title = "Reports & Analytics")
        }
        
        composable("settings") {
            PlaceholderScreen(title = "Settings")
        }
        
        // Demo Data Separation Screen
        composable("demo_data_separation") {
            DemoDataSeparationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title - Coming Soon")
    }
}
