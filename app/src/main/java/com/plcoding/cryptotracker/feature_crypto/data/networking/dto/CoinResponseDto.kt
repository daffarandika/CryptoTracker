package com.plcoding.cryptotracker.feature_crypto.data.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinResponseDto(
    val data: List<CoinDto>
)
