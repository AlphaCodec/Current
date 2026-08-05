package com.current.news.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import com.current.news.ui.components.AppTab
import com.current.news.ui.components.BottomNavBar
import com.current.news.ui.screens.*
import com.current.news.viewmodel.NewsViewModel

private const val ROUTE_ARTICLE = "article/{articleId}"

@Composable
fun CurrentNavGraph() {
    val navController = rememberNavController()
    val viewModel: NewsViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in AppTab.values().map { it.route }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = AppTab.Home.route) {
                composable(AppTab.Home.route) {
                    HomeScreen(viewModel = viewModel, onOpenArticle = { id ->
                        navController.navigate("article/$id")
                    })
                }
                composable(AppTab.Explore.route) {
                    ExploreScreen(onOpenSearch = { navController.navigate("search") })
                }
                composable(AppTab.Saved.route) {
                    SavedScreen(viewModel = viewModel, onOpenArticle = { id ->
                        navController.navigate("article/$id")
                    })
                }
                composable(AppTab.Profile.route) {
                    ProfileScreen()
                }
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenArticle = { id -> navController.navigate("article/$id") }
                    )
                }
                composable(ROUTE_ARTICLE) { entry ->
                    val id = entry.arguments?.getString("articleId") ?: return@composable
                    ArticleScreen(
                        articleId = id,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showBottomBar) {
            BottomNavBar(
                current = AppTab.values().firstOrNull { it.route == currentRoute } ?: AppTab.Home,
                onSelect = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(AppTab.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
