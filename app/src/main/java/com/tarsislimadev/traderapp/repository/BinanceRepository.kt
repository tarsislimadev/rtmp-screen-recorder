package com.tarsislimadev.traderapp.repository

import android.util.Log
import com.google.gson.Gson
import com.tarsislimadev.traderapp.api.BinanceService
import com.tarsislimadev.traderapp.model.BinanceWebSocketTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BinanceRepository {
    private val client = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.binance.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(BinanceService::class.java)
    private val gson = Gson()

    private val _tickerFlow = MutableStateFlow<BinanceWebSocketTicker?>(null)
    val tickerFlow: StateFlow<BinanceWebSocketTicker?> = _tickerFlow

    private var webSocket: WebSocket? = null

    fun startTickerWebSocket(symbol: String) {
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/${symbol.lowercase()}@ticker")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ticker = gson.fromJson(text, BinanceWebSocketTicker::class.java)
                    _tickerFlow.value = ticker
                } catch (e: Exception) {
                    Log.e("BinanceRepo", "Error parsing WS message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("BinanceRepo", "WS Failure", t)
            }
        })
    }

    fun stopTickerWebSocket() {
        webSocket?.close(1000, "User requested")
    }

    suspend fun getAccount(apiKey: String, secretKey: String): Any? {
        val timestamp = System.currentTimeMillis()
        val query = "timestamp=$timestamp"
        val signature = hmacSha256(query, secretKey)
        return try {
            service.getAccount(timestamp, signature, apiKey)
        } catch (e: Exception) {
            Log.e("BinanceRepo", "Error fetching account", e)
            null
        }
    }

    suspend fun createMarketBuyOrder(
        symbol: String,
        quantity: String,
        apiKey: String,
        secretKey: String
    ): Any? {
        val timestamp = System.currentTimeMillis()
        val side = "BUY"
        val type = "MARKET"
        val query = "symbol=$symbol&side=$side&type=$type&quantity=$quantity&timestamp=$timestamp"
        val signature = hmacSha256(query, secretKey)
        
        return try {
            service.createOrder(symbol, side, type, quantity, timestamp, signature, apiKey)
        } catch (e: Exception) {
            Log.e("BinanceRepo", "Error creating order", e)
            null
        }
    }

    private fun hmacSha256(message: String, secret: String): String {
        val hmacKey = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val hmac = javax.crypto.Mac.getInstance("HmacSHA256")
        hmac.init(hmacKey)
        val hash = hmac.doFinal(message.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
