package dev.shorthouse.cryptodata.ui.screen.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.shorthouse.cryptodata.R
import dev.shorthouse.cryptodata.model.Coin
import dev.shorthouse.cryptodata.ui.component.LoadingIndicator
import dev.shorthouse.cryptodata.ui.previewdata.CoinListUiStatePreviewProvider
import dev.shorthouse.cryptodata.ui.screen.Screen
import dev.shorthouse.cryptodata.ui.screen.list.component.CoinFavouriteItem
import dev.shorthouse.cryptodata.ui.screen.list.component.CoinListItem
import dev.shorthouse.cryptodata.ui.theme.AppTheme

@Composable
fun CoinListScreen(
    navController: NavController,
    viewModel: CoinListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CoinListScreen(
        uiState = uiState,
        onItemClick = { coin ->
            navController.navigate(Screen.DetailScreen.route + "/${coin.id}")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    uiState: CoinListUiState,
    onItemClick: (Coin) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (uiState) {
        is CoinListUiState.Success -> {
            Scaffold(
                topBar = {
                    CoinListTopBar(
                        scrollBehavior = scrollBehavior
                    )
                },
                content = { scaffoldPadding ->
                    CoinListContent(
                        coins = uiState.coins,
                        onItemClick = onItemClick,
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                },
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }
        is CoinListUiState.Loading -> LoadingIndicator()
        is CoinListUiState.Error -> Text("error")
    }
}

@Composable
private fun CoinListContent(
    coins: List<Coin>,
    modifier: Modifier,
    onItemClick: (Coin) -> Unit
) {
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "Favourites",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = 3,
                itemContent = { index ->
                    CoinFavouriteItem(coin = coins[index])
                }
            )
//            CoinFavouriteItem(coin = coins[0])
//            CoinFavouriteItem(coin = coins[1])
//            CoinFavouriteItem(coin = coins[2])
        }

        Text(
            text = "Coins",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = coins.size,
                key = { coins[it].id },
                itemContent = { index ->
                    val coinListItem = coins[index]

                    CoinListItem(
                        coin = coinListItem,
                        onItemClick = { onItemClick(coinListItem) }
                    )
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CoinListTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.top_app_bar_title_market),
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary
        ),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.background(Color.Green)
    )
}

@Composable
@Preview(showBackground = true)
private fun ListScreenPreview(
    @PreviewParameter(CoinListUiStatePreviewProvider::class) uiState: CoinListUiState
) {
    AppTheme {
        CoinListScreen(
            uiState = uiState,
            onItemClick = {}
        )
    }
}
