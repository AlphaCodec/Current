package com.current.news.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.current.news.ui.components.AppTab
import com.current.news.ui.components.BottomNavBar
import com.current.news.ui.screens.*
import com.current.news.viewmodel.NewsViewModel
import com.current.news.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private const val ROUTE_ARTICLE = "article/{articleId}"
private const val ROUTE_MAIN = "main"

@Composable
fun CurrentNavGraph(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val viewModel: NewsViewModel = viewModel()

    // Bridges Profile → Reading preferences → Country into the shared feed
    // ViewModel, wherever it changes. Centralized here (rather than in each
    // screen) so switching countries doesn't trigger a reload once per
    // mounted screen.
    val countryCode by settingsViewModel.countryCode.collectAsState()
    LaunchedEffect(countryCode) {
        viewModel.setCountry(countryCode)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onMainPager = currentRoute == ROUTE_MAIN

    // Drives which of Home / World / Saved / Profile is showing. Swiping
    // left/right on the content area moves between them directly — this is
    // the actual mechanism, tapping a bottom nav icon just animates this
    // same pager to that page rather than doing a separate navigation.
    val pagerState = rememberPagerState(pageCount = { AppTab.values().size })
    val pagerScope = rememberCoroutineScope()

    val currentTab = if (onMainPager) {
        AppTab.values().getOrElse(pagerState.currentPage) { AppTab.Home }
    } else {
        AppTab.Home
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = ROUTE_MAIN) {
                composable(ROUTE_MAIN) {
                    HorizontalPager(
                        state = pagerState,
                        // Keeps all four tabs composed simultaneously instead
                        // of disposing off-screen ones — without this, swiping
                        // away from a tab and back would reset its scroll
                        // position back to the top every time.
                        beyondViewportPageCount = AppTab.values().size,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (AppTab.values()[page]) {
                            AppTab.Home -> HomeScreen(
                                viewModel = viewModel,
                                onOpenArticle = { id -> navController.navigate("article/$id") }
                            )
                            AppTab.World -> WorldScreen(
                                viewModel = viewModel,
                                onOpenSearch = { navController.navigate("search") },
                                onOpenArticle = { id -> navController.navigate("article/$id") }
                            )
                            AppTab.Saved -> SavedScreen(
                                viewModel = viewModel,
                                onOpenArticle = { id -> navController.navigate("article/$id") }
                            )
                            AppTab.Profile -> ProfileScreen(settingsViewModel = settingsViewModel)
                        }
                    }
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

        if (onMainPager) {
            BottomNavBar(
                current = currentTab,
                onSelect = { tab ->
                    val index = AppTab.values().indexOf(tab)
                    pagerScope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    }
}
