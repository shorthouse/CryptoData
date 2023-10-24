package dev.shorthouse.coinwatch.ui.screen.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.shorthouse.coinwatch.R
import dev.shorthouse.coinwatch.model.CoinChart
import dev.shorthouse.coinwatch.model.CoinDetails
import dev.shorthouse.coinwatch.ui.component.ErrorState
import dev.shorthouse.coinwatch.ui.model.ChartPeriod
import dev.shorthouse.coinwatch.ui.previewdata.CoinDetailsUiStatePreviewProvider
import dev.shorthouse.coinwatch.ui.screen.details.component.CoinChartCard
import dev.shorthouse.coinwatch.ui.screen.details.component.CoinChartRangeCard
import dev.shorthouse.coinwatch.ui.screen.details.component.CoinDetailsSkeletonLoader
import dev.shorthouse.coinwatch.ui.screen.details.component.MarketStatsCard
import dev.shorthouse.coinwatch.ui.theme.AppTheme

@Composable
fun CoinDetailsScreen(
    navController: NavController,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CoinDetailsScreen(
        uiState = uiState,
        onNavigateUp = { navController.navigateUp() },
        onClickFavouriteCoin = { viewModel.toggleIsCoinFavourite() },
        onClickChartPeriod = { viewModel.updateChartPeriod(it) },
        onRefresh = { viewModel.initialiseUiState() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailsScreen(
    uiState: DetailsUiState,
    onNavigateUp: () -> Unit,
    onClickFavouriteCoin: () -> Unit,
    onClickChartPeriod: (ChartPeriod) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (uiState) {
        is DetailsUiState.Success -> {
            Scaffold(
                topBar = {
                    CoinDetailsTopBar(
                        coinDetails = uiState.coinDetails,
                        isCoinFavourite = uiState.isCoinFavourite,
                        onNavigateUp = onNavigateUp,
                        onClickFavouriteCoin = onClickFavouriteCoin,
                        scrollBehavior = scrollBehavior
                    )
                },
                content = { scaffoldPadding ->
                    CoinDetailsContent(
                        coinDetails = uiState.coinDetails,
                        coinChart = uiState.coinChart,
                        chartPeriod = uiState.chartPeriod,
                        onClickChartPeriod = onClickChartPeriod,
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                },
                modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }

        is DetailsUiState.Loading -> {
            CoinDetailsSkeletonLoader()
        }

        is DetailsUiState.Error -> {
            ErrorState(
                message = uiState.message,
                onRetry = onRefresh,
                onNavigateUp = onNavigateUp
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CoinDetailsTopBar(
    coinDetails: CoinDetails,
    isCoinFavourite: Boolean,
    onNavigateUp: () -> Unit,
    onClickFavouriteCoin: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageBuilder = remember(context) {
        ImageRequest.Builder(context = context)
            .decoderFactory(factory = SvgDecoder.Factory())
    }

    LargeTopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_top_bar_back)
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = coinDetails.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = coinDetails.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AsyncImage(
                    model = imageBuilder
                        .data(coinDetails.imageUrl)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(44.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onClickFavouriteCoin) {
                Icon(
                    imageVector = if (isCoinFavourite) {
                        Icons.Rounded.Star
                    } else {
                        Icons.Rounded.StarOutline
                    },
                    contentDescription = stringResource(R.string.cd_top_bar_favourite),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@Composable
private fun CoinDetailsContent(
    coinDetails: CoinDetails,
    coinChart: CoinChart,
    chartPeriod: ChartPeriod,
    onClickChartPeriod: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .testTag("coin_details_content")
    ) {
        CoinChartCard(
            currentPrice = coinDetails.currentPrice,
            prices = coinChart.prices,
            periodPriceChangePercentage = coinChart.periodPriceChangePercentage,
            chartPeriod = chartPeriod,
            onClickChartPeriod = onClickChartPeriod
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.title_chart_range),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        CoinChartRangeCard(
            currentPrice = coinDetails.currentPrice,
            minPrice = coinChart.minPrice,
            maxPrice = coinChart.maxPrice,
            isPricesEmpty = coinChart.prices.isEmpty()
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.card_header_market_stats),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        MarketStatsCard(coinDetails = coinDetails)
    }
}

@Composable
@Preview
private fun CoinDetailScreenPreview(
    @PreviewParameter(CoinDetailsUiStatePreviewProvider::class) uiState: DetailsUiState
) {
    AppTheme {
        CoinDetailsScreen(
            uiState = uiState,
            onNavigateUp = {},
            onClickFavouriteCoin = {},
            onClickChartPeriod = {},
            onRefresh = {}
        )
    }
}
