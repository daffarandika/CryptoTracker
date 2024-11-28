package com.plcoding.cryptotracker.feature_crypto.presentation.coin_detail

import android.icu.text.NumberFormat
import java.util.Locale

data class ValueLabel(
    val value: Float,
    val unit: String
) {
    fun format(): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            val fractionDigit = when {
                value > 1000 -> 0
                value in 2f .. 999f -> 2
                else -> 3
            }
            maximumFractionDigits = fractionDigit
            minimumFractionDigits = 0
        }
        return "${formatter.format(value)}$unit"
    }
}
