package dev.shorthouse.coinwatch.ui.screen.favourites

import com.google.common.truth.Truth.assertThat
import dev.shorthouse.coinwatch.MainDispatcherRule
import dev.shorthouse.coinwatch.R
import dev.shorthouse.coinwatch.common.Result
import dev.shorthouse.coinwatch.data.source.local.model.FavouriteCoin
import dev.shorthouse.coinwatch.data.source.local.model.FavouriteCoinId
import dev.shorthouse.coinwatch.data.userPreferences.UserPreferences
import dev.shorthouse.coinwatch.domain.GetFavouriteCoinIdsUseCase
import dev.shorthouse.coinwatch.domain.GetFavouriteCoinsUseCase
import dev.shorthouse.coinwatch.domain.GetUserPreferencesUseCase
import dev.shorthouse.coinwatch.domain.UpdateCachedFavouriteCoinsUseCase
import dev.shorthouse.coinwatch.domain.UpdateIsFavouritesCondensedUseCase
import dev.shorthouse.coinwatch.model.Percentage
import dev.shorthouse.coinwatch.model.Price
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class FavouritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Class under test
    private lateinit var viewModel: FavouritesViewModel

    @RelaxedMockK
    private lateinit var getFavouriteCoinsUseCase: GetFavouriteCoinsUseCase

    @RelaxedMockK
    private lateinit var updateCachedFavouriteCoinsUseCase: UpdateCachedFavouriteCoinsUseCase

    @RelaxedMockK
    private lateinit var getFavouriteCoinIdsUseCase: GetFavouriteCoinIdsUseCase

    @RelaxedMockK
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase

    @RelaxedMockK
    private lateinit var updateIsFavouritesCondensedUseCase: UpdateIsFavouritesCondensedUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        viewModel = FavouritesViewModel(
            getFavouriteCoinsUseCase = getFavouriteCoinsUseCase,
            updateCachedFavouriteCoinsUseCase = updateCachedFavouriteCoinsUseCase,
            getFavouriteCoinIdsUseCase = getFavouriteCoinIdsUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updateIsFavouritesCondensedUseCase = updateIsFavouritesCondensedUseCase
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When ViewModel is created should have loading UI state`() {
        // Arrange
        val expectedUiState = FavouritesUiState(isLoading = true)

        // Act
        val uiState = viewModel.uiState.value

        // Assert
        assertThat(uiState).isEqualTo(expectedUiState)
    }

    @Test
    fun `When favourite coin ids returns error should add error message`() {
        // Arrange
        every {
            getFavouriteCoinIdsUseCase()
        } returns flowOf(Result.Error("Favourite coin IDs error"))

        every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())

        // Act
        viewModel.initialiseUiState()

        // Assert
        assertThat(viewModel.uiState.value.errorMessageIds)
            .contains(R.string.error_local_favourite_coin_ids)
    }

    @Test
    fun `When update cached coins returns error should add error message`() {
        // Arrange
        val favouriteCoinIds = listOf(FavouriteCoinId("Bitcoin"))
        val userPreferences = UserPreferences()

        every { getFavouriteCoinIdsUseCase() } returns flowOf(Result.Success(favouriteCoinIds))
        every { getUserPreferencesUseCase() } returns flowOf(userPreferences)
        coEvery {
            updateCachedFavouriteCoinsUseCase(
                coinIds = favouriteCoinIds,
                currency = userPreferences.currency,
                coinSort = userPreferences.coinSort
            )
        } returns Result.Error("Update cached favourite coins error")

        // Act
        viewModel.initialiseUiState()

        // Assert
        assertThat(viewModel.uiState.value.errorMessageIds)
            .contains(R.string.error_network_favourite_coins)
    }

    @Test
    fun `When update cached coins returns success should not add error`() {
        // Arrange
        val favouriteCoinIds = listOf(FavouriteCoinId("Bitcoin"))
        val userPreferences = UserPreferences()

        every { getFavouriteCoinIdsUseCase() } returns flowOf(Result.Success(favouriteCoinIds))
        every { getUserPreferencesUseCase() } returns flowOf(userPreferences)
        coEvery {
            updateCachedFavouriteCoinsUseCase(
                coinIds = favouriteCoinIds,
                currency = userPreferences.currency,
                coinSort = userPreferences.coinSort
            )
        } returns Result.Success(emptyList())

        // Act
        viewModel.initialiseUiState()

        // Assert
        assertThat(viewModel.uiState.value.errorMessageIds)
            .doesNotContain(R.string.error_network_favourite_coins)
    }

    @Test
    fun `When get favourite coins returns error should add error message and stop loading`() {
        // Arrange
        val expectedUiState = FavouritesUiState(
            isLoading = false,
            errorMessageIds = listOf(R.string.error_local_favourite_coins)
        )
        every { getFavouriteCoinsUseCase() } returns flowOf(Result.Error("Favourite coins error"))

        // Act
        viewModel.initialiseUiState()

        // Assert
        assertThat(viewModel.uiState.value).isEqualTo(expectedUiState)
    }

    @Test
    fun `When get favourite coins returns success should update coins and stop loading`() {
        // Arrange
        val favouriteCoins = listOf(
            FavouriteCoin(
                id = "bitcoin",
                symbol = "BTC",
                name = "Bitcoin",
                imageUrl = "https://cdn.coinranking.com/bOabBYkcX/bitcoin_btc.svg",
                currentPrice = Price("29446.336548759988"),
                priceChangePercentage24h = Percentage("1.76833"),
                prices24h = persistentListOf(
                    BigDecimal("29390.15178296929"),
                    BigDecimal("29428.222505493162"),
                    BigDecimal("29475.12359313808"),
                    BigDecimal("29471.20179209623")
                )
            )
        )

        val expectedUiState = FavouritesUiState(
            favouriteCoins = favouriteCoins.toPersistentList(),
            isLoading = false,
        )

        every { getFavouriteCoinsUseCase() } returns flowOf(Result.Success(favouriteCoins))

        // Act
        viewModel.initialiseUiState()

        // Assert
        assertThat(viewModel.uiState.value).isEqualTo(expectedUiState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When pull refreshing favourite coins with coin ids error should add error message`() =
        runTest {
            // Arrange
            every {
                getFavouriteCoinIdsUseCase()
            } returns flowOf(Result.Error("Favourite coin IDs error"))
            every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())

            // Act
            viewModel.pullRefreshFavouriteCoins()

            // Assert
            assertThat(viewModel.uiState.value.isRefreshing).isTrue()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.isRefreshing).isFalse()

            assertThat(viewModel.uiState.value.errorMessageIds)
                .contains(R.string.error_local_favourite_coin_ids)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When pull refreshing favourite coins with success should call update cached favourite coins`() =
        runTest {
            // Arrange
            val favouriteCoinIds = listOf(FavouriteCoinId("Bitcoin"))
            val userPreferences = UserPreferences()

            every { getFavouriteCoinIdsUseCase() } returns flowOf(Result.Success(favouriteCoinIds))
            every { getUserPreferencesUseCase() } returns flowOf(userPreferences)

            // Act
            viewModel.pullRefreshFavouriteCoins()

            // Assert
            assertThat(viewModel.uiState.value.isRefreshing).isTrue()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.isRefreshing).isFalse()

            coVerify {
                updateCachedFavouriteCoinsUseCase(
                    coinIds = favouriteCoinIds,
                    currency = userPreferences.currency,
                    coinSort = userPreferences.coinSort
                )
            }

            confirmVerified(updateCachedFavouriteCoinsUseCase)
        }

    @Test
    fun `When dismissing error should remove specified error from list`() {
        // Arrange
        every { getFavouriteCoinsUseCase() } returns flowOf(Result.Error("Favourite coins error"))

        // Act
        viewModel.initialiseUiState()

        val errorIsInserted = viewModel.uiState.value.errorMessageIds.isNotEmpty()
        val errorMessageId = viewModel.uiState.value.errorMessageIds.first()
        viewModel.dismissErrorMessage(errorMessageId)

        // Assert
        assertThat(errorIsInserted).isTrue()
        assertThat(viewModel.uiState.value.errorMessageIds).doesNotContain(errorMessageId)
    }
}
