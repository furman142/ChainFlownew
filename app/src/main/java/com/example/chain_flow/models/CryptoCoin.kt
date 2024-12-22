package com.example.chain_flow.models

import com.example.chain_flow.R
data class CryptoCoin(
    val cryptoName: String,
    val cryptoValue: String,
    val imageUrl: String,
    var watchlist: Boolean = false,
    val description: String = "",
    val rawPrice: Double
) {
    fun calculateAmountFromUSD(amountInUSD: Double): Double {
        return amountInUSD / rawPrice
    }
}

