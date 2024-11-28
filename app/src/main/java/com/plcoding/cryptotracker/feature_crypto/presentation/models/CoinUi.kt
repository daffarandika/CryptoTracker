package com.plcoding.cryptotracker.feature_crypto.presentation.models

import androidx.annotation.DrawableRes
import com.plcoding.cryptotracker.feature_crypto.domain.Coin
import android.icu.text.NumberFormat
import com.plcoding.cryptotracker.core.presentation.util.getDrawableIdForCoin
import com.plcoding.cryptotracker.feature_crypto.domain.CoinPrice
import com.plcoding.cryptotracker.feature_crypto.presentation.coin_detail.DataPoint
import java.util.Locale

data class CoinUi(
    val id: String,
    val rank: Int,
    val name: String,
    val symbol: String,
    val marketCapsUsd: DisplayableNumber,
    val priceUsd: DisplayableNumber,
    val changePercent24Hr: DisplayableNumber,
    val coinPriceHistory: List<DataPoint> = emptyList(),
    @DrawableRes val iconRes: Int,
)

data class DisplayableNumber(
    val value: Double,
    val formatted: String,
)

fun Coin.toCoinUi(): CoinUi {
    return CoinUi(
        id = this.id,
        name = this.name,
        symbol = this.symbol,
        rank = this.rank,
        priceUsd = this.priceUsd.toDisplayableNumber(),
        marketCapsUsd = this.marketCapUsd.toDisplayableNumber(),
        changePercent24Hr = this.changePercent24Hr.toDisplayableNumber(),
        iconRes = getDrawableIdForCoin(symbol = this.symbol)
    )
}

fun Double.toDisplayableNumber(): DisplayableNumber {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return DisplayableNumber(
        value = this,
        formatted = formatter.format(this)
    )
}