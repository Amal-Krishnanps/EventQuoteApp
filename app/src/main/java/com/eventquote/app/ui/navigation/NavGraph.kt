package com.eventquote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eventquote.app.ui.screens.about.AboutScreen
import com.eventquote.app.ui.screens.backup.BackupScreen
import com.eventquote.app.ui.screens.company.CompanySettingsScreen
import com.eventquote.app.ui.screens.dashboard.DashboardScreen
import com.eventquote.app.ui.screens.estimate.NewEstimateScreen
import com.eventquote.app.ui.screens.master.ServiceMasterScreen
import com.eventquote.app.ui.screens.saved.SavedEstimatesScreen
import com.eventquote.app.viewmodel.BackupViewModel
import com.eventquote.app.viewmodel.CompanyViewModel
import com.eventquote.app.viewmodel.DashboardViewModel
import com.eventquote.app.viewmodel.EstimateViewModel
import com.eventquote.app.viewmodel.SavedEstimatesViewModel
import com.eventquote.app.viewmodel.ServiceMasterViewModel
import com.eventquote.app.viewmodel.ViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    factory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier
    ) {
        // ---- Dashboard ----
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel(factory = factory),
                onNewEstimate = { navController.navigate(Routes.NEW_ESTIMATE) },
                onSavedEstimates = { navController.navigate(Routes.SAVED_ESTIMATES) },
                onCompanySettings = { navController.navigate(Routes.COMPANY_SETTINGS) },
                onServicesMaster = { navController.navigate(Routes.SERVICES_MASTER) },
                onBackup = { navController.navigate(Routes.BACKUP) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onEstimateClick = { id -> navController.navigate(Routes.editEstimate(id)) }
            )
        }

        // ---- Company Settings ----
        composable(Routes.COMPANY_SETTINGS) {
            val vm: CompanyViewModel = viewModel(factory = factory)
            CompanySettingsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---- New Estimate ----
        composable(Routes.NEW_ESTIMATE) {
            val vm: EstimateViewModel = viewModel(factory = factory)
            NewEstimateScreen(
                viewModel = vm,
                estimateId = null,
                onNavigateBack = { navController.popBackStack() },
                onEstimateSaved = { navController.popBackStack() }
            )
        }

        // ---- Edit Estimate ----
        composable(
            route = Routes.EDIT_ESTIMATE,
            arguments = listOf(
                navArgument("estimateId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val estimateId = backStackEntry.arguments?.getString("estimateId")
            val vm: EstimateViewModel = viewModel(factory = factory)
            NewEstimateScreen(
                viewModel = vm,
                estimateId = estimateId,
                onNavigateBack = { navController.popBackStack() },
                onEstimateSaved = { navController.popBackStack() }
            )
        }

        // ---- Saved Estimates ----
        composable(Routes.SAVED_ESTIMATES) {
            val vm: SavedEstimatesViewModel = viewModel(factory = factory)
            SavedEstimatesScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onEstimateClick = { id -> navController.navigate(Routes.editEstimate(id)) },
                onNewEstimate = { navController.navigate(Routes.NEW_ESTIMATE) }
            )
        }

        // ---- Services Master ----
        composable(Routes.SERVICES_MASTER) {
            val vm: ServiceMasterViewModel = viewModel(factory = factory)
            ServiceMasterScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---- Backup & Restore ----
        composable(Routes.BACKUP) {
            val vm: BackupViewModel = viewModel(factory = factory)
            BackupScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---- About ----
        composable(Routes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
