package com.plcoding.cryptotracker.core.presentation.util

import java.time.ZoneId
import java.time.ZonedDateTime

fun ZonedDateTime.toEpochMilli(): Long {
    return this.withZoneSameInstant(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
}