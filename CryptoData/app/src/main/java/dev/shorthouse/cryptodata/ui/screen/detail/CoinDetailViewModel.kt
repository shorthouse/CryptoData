package dev.shorthouse.cryptodata.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shorthouse.cryptodata.common.Constants.PARAM_COIN_ID
import dev.shorthouse.cryptodata.common.Result
import dev.shorthouse.cryptodata.domain.GetCoinChartUseCase
import dev.shorthouse.cryptodata.domain.GetCoinDetailUseCase
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import timber.log.Timber

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCoinDetailUseCase: GetCoinDetailUseCase,
    private val getCoinChartUseCase: GetCoinChartUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<CoinDetailUiState>(CoinDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val chartPeriodFlow = MutableStateFlow(7.days)

    init {
        savedStateHandle.get<String>(PARAM_COIN_ID)?.let { coinId ->
            getCoinDetail(coinId = coinId)
        }
    }

    fun updateChartPeriod(chartPeriod: Duration) {
        chartPeriodFlow.value = chartPeriod
    }

    private fun getCoinDetail(coinId: String) {
        val coinDetailFlow = getCoinDetailUseCase(coinId = coinId)
        val coinChartFlow = chartPeriodFlow.flatMapLatest {
            getCoinChartUseCase(
                coinId = coinId,
                chartPeriodDays = it.inWholeDays.toString()
            )
        }

        combine(coinDetailFlow, coinChartFlow) { coinDetailResult, coinChartResult ->
            when {
                coinDetailResult is Result.Error -> {
                    Timber.e("getCoinDetail error ${coinDetailResult.message}")
                    _uiState.update { CoinDetailUiState.Error(coinDetailResult.message) }
                }
                coinChartResult is Result.Error -> {
                    Timber.e("getCoinChart error ${coinChartResult.message}")
                    _uiState.update { CoinDetailUiState.Error(coinChartResult.message) }
                }
                coinDetailResult is Result.Success && coinChartResult is Result.Success -> {
                    _uiState.update {
                        CoinDetailUiState.Success(
                            coinDetail = coinDetailResult.data,
                            coinChart = coinChartResult.data,
                            chartPeriod = chartPeriodFlow.value
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}
