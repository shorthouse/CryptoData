package dev.shorthouse.coinwatch.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import dev.shorthouse.coinwatch.model.SearchCoin
import dev.shorthouse.coinwatch.ui.screen.search.SearchScreen
import dev.shorthouse.coinwatch.ui.screen.search.SearchUiState
import dev.shorthouse.coinwatch.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class CoinSearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun when_uiStateLoading_should_showSkeletonLoader() {
        val uiStateError = SearchUiState.Error("Error message")

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateError,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("An error has occurred").assertIsDisplayed()
            onNodeWithText("Error message").assertIsDisplayed()
            onNodeWithText("Retry").assertIsDisplayed()
            onNodeWithText("Retry").assertHasClickAction()
        }
    }

    @Test
    fun when_uiStateErrorRetryClicked_should_callOnRefresh() {
        var onRefreshCalled = false
        val uiStateError = SearchUiState.Error("Error message")

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateError,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = { onRefreshCalled = true }
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("Retry").performClick()
        }

        assertThat(onRefreshCalled).isTrue()
    }

    @Test
    fun when_uiStateSuccess_should_showExpectedContent() {
        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithContentDescription("Back").assertIsDisplayed()
            onNodeWithText("Search coins").assertIsDisplayed()
        }
    }

    @Test
    fun when_searchQueryEntered_should_displaySearchQuery() {
        val searchQuery = "Bitcoin"

        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = searchQuery,
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("Bitcoin").assertIsDisplayed()
        }
    }

    @Test
    fun when_searchQueryEntered_should_displayClearSearchButton() {
        val searchQuery = "Bitcoin"

        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = searchQuery,
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithContentDescription("Clear search").assertIsDisplayed()
        }
    }

    @Test
    fun when_clearSearchClicked_should_clearSearchQuery() {
        val searchQuery = mutableStateOf("Bitcoin")

        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = { searchQuery.value = it },
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithContentDescription("Clear search").performClick()
            onNodeWithText("Search coins").assertIsDisplayed()
            onNodeWithText("Bitcoin").assertDoesNotExist()
        }
    }

    @Test
    fun when_typingInSearchBar_should_updateSearchQuery() {
        val searchQuery = mutableStateOf("")

        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = { searchQuery.value = it },
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("Search coins").performClick()
            onNodeWithText("Bitcoin").assertDoesNotExist()
            onNodeWithText("Search coins").performTextInput("Bitcoin")
            onNodeWithText("Bitcoin").assertIsDisplayed()
        }
    }

    @Test
    fun when_searchResultsListDisplayed_should_displayInExpectedFormat() {
        val searchResults = persistentListOf(
            SearchCoin(
                id = "Qwsogvtv82FCd",
                symbol = "BTC",
                name = "Bitcoin",
                imageUrl = "https://cdn.coinranking.com/bOabBYkcX/bitcoin_btc.svg"
            ),
            SearchCoin(
                id = "ZlZpzOJo43mIo",
                symbol = "BCH",
                name = "Bitcoin Cash",
                imageUrl = "https://cdn.coinranking.com/By8ziihX7/bch.svg"
            )
        )

        val uiStateSuccess = SearchUiState.Success(
            searchResults = searchResults,
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("Bitcoin").assertIsDisplayed()
            onNodeWithText("BTC").assertIsDisplayed()
            onNodeWithText("Bitcoin Cash").assertIsDisplayed()
            onNodeWithText("BCH").assertIsDisplayed()
        }
    }

    @Test
    fun when_clickingCoinItem_should_callOnCoinClick() {
        var onCoinClickCalled = false

        val searchResults = persistentListOf(
            SearchCoin(
                id = "Qwsogvtv82FCd",
                symbol = "BTC",
                name = "Bitcoin",
                imageUrl = "https://cdn.coinranking.com/bOabBYkcX/bitcoin_btc.svg"
            )
        )

        val uiStateSuccess = SearchUiState.Success(
            searchResults = searchResults,
            queryHasNoResults = false
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onCoinClick = { onCoinClickCalled = true },
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("Bitcoin").performClick()
        }

        assertThat(onCoinClickCalled).isTrue()
    }

    @Test
    fun when_searchQueryHasNoResults_should_displaySearchEmptyState() {
        val uiStateSuccess = SearchUiState.Success(
            searchResults = persistentListOf(),
            queryHasNoResults = true
        )

        composeTestRule.setContent {
            AppTheme {
                SearchScreen(
                    uiState = uiStateSuccess,
                    searchQuery = "abcdefghijk",
                    onSearchQueryChange = {},
                    onCoinClick = {},
                    onRefresh = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithText("No coins found").assertIsDisplayed()
            onNodeWithText("Try adjusting your search").assertIsDisplayed()
        }
    }
}
