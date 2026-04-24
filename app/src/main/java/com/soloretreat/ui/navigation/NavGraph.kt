package com.soloretreat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.soloretreat.ui.integration.FeedbackScreen
import com.soloretreat.ui.integration.JournalEntryScreen
import com.soloretreat.ui.integration.JournalListScreen
import com.soloretreat.ui.integration.RetreatSummaryScreen
import com.soloretreat.ui.preparation.ChantCatalogScreen
import com.soloretreat.ui.preparation.ChecklistScreen
import com.soloretreat.ui.preparation.EightPreceptsScreen
import com.soloretreat.ui.preparation.PreparationDashboardScreen
import com.soloretreat.ui.preparation.ScheduleBuilderScreen
import com.soloretreat.ui.preparation.TalkCatalogScreen
import com.soloretreat.ui.retreat.MealLogScreen
import com.soloretreat.ui.retreat.PreceptTrackerScreen
import com.soloretreat.ui.retreat.RetreatDashboardScreen
import com.soloretreat.ui.retreat.TimerScreen

@Composable
fun HomeRetreatNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Preparation routes
        composable(Screen.PreparationDashboard.route) {
            PreparationDashboardScreen(
                onScheduleBuilder = { navController.navigate(Screen.ScheduleBuilder.route) },
                onChantCatalog = { navController.navigate(Screen.ChantCatalog.route) },
                onTalkCatalog = { navController.navigate(Screen.TalkCatalog.route) },
                onChecklist = { navController.navigate(Screen.Checklist.route) },
                onEightPrecepts = { navController.navigate(Screen.EightPrecepts.route) },
                onStartRetreat = { navController.navigate(Screen.RetreatDashboard.route) {
                    popUpTo(Screen.PreparationDashboard.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.ScheduleBuilder.route) {
            ScheduleBuilderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TalkCatalog.route) {
            TalkCatalogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChantCatalog.route) {
            ChantCatalogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Checklist.route) {
            ChecklistScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EightPrecepts.route) {
            EightPreceptsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Retreat routes
        composable(Screen.RetreatDashboard.route) {
            RetreatDashboardScreen(
                onTimer = { activityType ->
                    navController.navigate(Screen.Timer.createRoute(activityType))
                },
                onPrecepts = { navController.navigate(Screen.PreceptTracker.route) },
                onMeal = { navController.navigate(Screen.MealLog.route) },
                onJournal = { navController.navigate(Screen.JournalList.route) },
                onEndRetreat = { navController.navigate(Screen.RetreatSummary.route) }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(navArgument("activityType") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            TimerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PreceptTracker.route) {
            PreceptTrackerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MealLog.route) {
            MealLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Integration routes
        composable(Screen.JournalList.route) {
            JournalListScreen(
                onNewEntry = { navController.navigate(Screen.JournalEntry.route) },
                onViewEntry = { entryId ->
                    navController.navigate(Screen.JournalEntry.createRoute(entryId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.JournalEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            JournalEntryScreen(
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RetreatSummary.route) {
            RetreatSummaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onFeedback = { navController.navigate(Screen.Feedback.route) },
                onDone = {
                    navController.navigate(Screen.PreparationDashboard.route) {
                        popUpTo(Screen.RetreatSummary.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Feedback.route) {
            FeedbackScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object PreparationDashboard : Screen("preparation_dashboard")
    data object ScheduleBuilder : Screen("schedule_builder")
    data object ChantCatalog : Screen("chant_catalog")
    data object TalkCatalog : Screen("talk_catalog")
    data object Checklist : Screen("checklist")
    data object EightPrecepts : Screen("eight_precepts")

    data object RetreatDashboard : Screen("retreat_dashboard")
    data object Timer : Screen("timer?activityType={activityType}") {
        fun createRoute(activityType: String? = null): String {
            return if (activityType != null) "timer?activityType=$activityType" else "timer"
        }
    }
    data object PreceptTracker : Screen("precept_tracker")
    data object MealLog : Screen("meal_log")

    data object JournalList : Screen("journal_list")
    data object JournalEntry : Screen("journal_entry?entryId={entryId}") {
        fun createRoute(entryId: String? = null): String {
            return if (entryId != null) "journal_entry?entryId=$entryId" else "journal_entry"
        }
    }
    data object RetreatSummary : Screen("retreat_summary")
    data object Feedback : Screen("feedback")
}