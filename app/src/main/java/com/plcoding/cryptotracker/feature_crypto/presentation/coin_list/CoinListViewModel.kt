package com.plcoding.cryptotracker.feature_crypto.presentation.coin_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plcoding.cryptotracker.core.domain.util.onError
import com.plcoding.cryptotracker.core.domain.util.onSuccess
import com.plcoding.cryptotracker.feature_crypto.domain.CoinDataSource
import com.plcoding.cryptotracker.feature_crypto.presentation.coin_detail.DataPoint
import com.plcoding.cryptotracker.feature_crypto.presentation.models.CoinUi
import com.plcoding.cryptotracker.feature_crypto.presentation.models.toCoinUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CoinListViewModel(
    private val coinDataSource: CoinDataSource
): ViewModel() {

    private val _state = MutableStateFlow(CoinListState())

        // The `stateIn` operator is used to share the `_state` Flow among multiple subscribers.
        // The `SharingStarted.WhileSubscribed(5000L)` strategy ensures that the Flow is shared for up to 5 seconds after the last subscription.
        // This helps to optimize resource usage by avoiding unnecessary updates and data fetching.    val state = _state
    val state = _state
        .onStart {
            loadCoins()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            CoinListState()
        )

    private val _event = Channel<CoinListEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadCoins()
    }

    fun onAction(action: CoinListAction) {
        when (action) {
            is CoinListAction.OnCoinClick -> {
                selectCoin(action.coinUi)
            }
        }
    }

    private fun selectCoin(coinUi: CoinUi){
        _state.update { it.copy(selectedCoin = coinUi) }
        viewModelScope.launch {
            coinDataSource.getCoinHistory(
                coinId = coinUi.id,
                start = ZonedDateTime.now().minusDays(5L),
                end = ZonedDateTime.now()
            ).onSuccess { history ->
                val dataPoints = history
                    .sortedBy { it.dateTime }
                    .map {
                        DataPoint(
                            x = it.dateTime.hour.toFloat(),
                            y = it.priceUsd.toFloat(),
                            xLabel = DateTimeFormatter
                                .ofPattern("ha\nM/d")
                                .format(it.dateTime)
                        )
                    }
                _state.update {
                    it.copy(selectedCoin = it.selectedCoin?.copy(
                        coinPriceHistory = dataPoints
                    ))
                }
            }
        }
    }

    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true
            ) }

            coinDataSource
                .getCoins()
                .onSuccess { coins ->
                    _state.update { it.copy(
                        coins = coins.map { it.toCoinUi() },
                        isLoading = false
                    )}
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = true) }
                    _event.send(CoinListEvent.Error(error))
                }
        }
    }
}