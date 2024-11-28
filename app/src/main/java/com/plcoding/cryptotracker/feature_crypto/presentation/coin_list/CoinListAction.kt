package com.plcoding.cryptotracker.feature_crypto.presentation.coin_list

import com.plcoding.cryptotracker.feature_crypto.presentation.models.CoinUi

sealed interface CoinListAction {
    data class OnCoinClick(val coinUi: CoinUi): CoinListAction
//    data object OnRefresh: CoinListAction
}