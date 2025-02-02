package com.example.chain_flow.models

data class CoinMarketCapResponse(
    val status: Status,
    val data: List<CryptoData>
)

data class Status(
    val timestamp: String,
    val error_code: Int,
    val error_message: String?
)

data class CryptoData(
    val id: Int,
    val name: String,
    val symbol: String,
    val quote: Quote
)

data class Quote(
    val USD: UsdData
)

data class UsdData(
    val price: Double,
    val percent_change_24h: Double
) 