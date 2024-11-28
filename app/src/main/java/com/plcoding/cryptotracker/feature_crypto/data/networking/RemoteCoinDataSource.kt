package com.plcoding.cryptotracker.feature_crypto.data.networking

import com.plcoding.cryptotracker.core.data.networking.constructUrl
import com.plcoding.cryptotracker.core.data.networking.safeCall
import com.plcoding.cryptotracker.core.domain.util.NetworkError
import com.plcoding.cryptotracker.core.domain.util.Result
import com.plcoding.cryptotracker.core.domain.util.map
import com.plcoding.cryptotracker.core.presentation.util.toEpochMilli
import com.plcoding.cryptotracker.feature_crypto.data.mappers.toCoin
import com.plcoding.cryptotracker.feature_crypto.data.mappers.toCoinPrice
import com.plcoding.cryptotracker.feature_crypto.data.networking.dto.CoinHistoryDto
import com.plcoding.cryptotracker.feature_crypto.data.networking.dto.CoinResponseDto
import com.plcoding.cryptotracker.feature_crypto.domain.Coin
import com.plcoding.cryptotracker.feature_crypto.domain.CoinDataSource
import com.plcoding.cryptotracker.feature_crypto.domain.CoinPrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.ZonedDateTime

class RemoteCoinDataSource(
    private val httpClient: HttpClient
): CoinDataSource {

    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        return safeCall<CoinResponseDto> {
            httpClient.get(
                urlString = constructUrl("/assets")
            )
        }.map { result ->
            result.data.map{ it.toCoin() }
        }
    }

    override suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError> {
        return safeCall<CoinHistoryDto> {
            httpClient.get(
                urlString = constructUrl("/assets/$coinId/history")
            ) {
                parameter("interval", "h6")
                parameter("start", start.toEpochMilli())
                parameter("end", end.toEpochMilli())
            }
        }.map { res ->
            res.data.map { it.toCoinPrice() }
        }
    }
}