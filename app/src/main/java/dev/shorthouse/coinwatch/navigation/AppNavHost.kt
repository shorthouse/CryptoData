package dev.shorthouse.coinwatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.shorthouse.coinwatch.ui.screen.detail.CoinDetailScreen
import dev.shorthouse.coinwatch.ui.screen.list.CoinListScreen
import dev.shorthouse.coinwatch.ui.screen.search.CoinSearchScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ScreenOld.CoinList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = ScreenOld.CoinList.route) {
            CoinListScreen(navController = navController)
        }
        composable(route = ScreenOld.CoinDetail.route + "/{coinId}") {
            CoinDetailScreen(navController = navController)
        }
        composable(route = ScreenOld.CoinSearch.route) {
            CoinSearchScreen(navController = navController)
        }
    }
}
