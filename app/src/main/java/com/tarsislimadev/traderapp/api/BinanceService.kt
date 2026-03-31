package com.tarsislimadev.traderapp.api

import com.tarsislimadev.traderapp.model.BinanceTicker
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceService {
    @GET("api/v3/ticker/price")
    suspend fun getTicker(@Query("symbol") symbol: String): BinanceTicker

    @GET("api/v3/account")
    suspend fun getAccount(
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
        @retrofit2.http.Header("X-MBX-APIKEY") apiKey: String
    ): Any // Placeholder model for now

    @retrofit2.http.POST("api/v3/order")
    suspend fun createOrder(
        @Query("symbol") symbol: String,
        @Query("side") side: String,
        @Query("type") type: String,
        @Query("quantity") quantity: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
        @retrofit2.http.Header("X-MBX-APIKEY") apiKey: String
    ): Any // Placeholder model for now
}
