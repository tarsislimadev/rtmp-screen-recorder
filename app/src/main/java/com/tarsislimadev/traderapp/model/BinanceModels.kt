package com.tarsislimadev.traderapp.model

import com.google.gson.annotations.SerializedName

data class BinanceTicker(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: String
)

data class BinanceWebSocketTicker(
    @SerializedName("e") val eventType: String,
    @SerializedName("s") val symbol: String,
    @SerializedName("c") val currentPrice: String
)
