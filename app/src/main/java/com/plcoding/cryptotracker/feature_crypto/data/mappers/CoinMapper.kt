package com.plcoding.cryptotracker.feature_crypto.data.mappers

import com.plcoding.cryptotracker.feature_crypto.data.networking.dto.CoinDto
import com.plcoding.cryptotracker.feature_crypto.data.networking.dto.CoinPriceDto
import com.plcoding.cryptotracker.feature_crypto.domain.Coin
import com.plcoding.cryptotracker.feature_crypto.domain.CoinPrice
import java.time.Instant
import java.time.ZoneId

fun CoinDto.toCoin(): Coin {
    return Coin(
        id = id,
        rank = rank,
        name = name,
        symbol = symbol,
        priceUsd = priceUsd,
        changePercent24Hr = changePercent24Hr,
        marketCapUsd = marketCapUsd
    )
}

fun CoinPriceDto.toCoinPrice(): CoinPrice{
    return CoinPrice(
        priceUsd = this.priceUsd,
        dateTime = Instant.ofEpochMilli(this.time)
            .atZone(ZoneId.systemDefault())
    )
}
