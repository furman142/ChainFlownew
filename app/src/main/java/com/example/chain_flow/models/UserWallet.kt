package com.example.chain_flow.models

data class UserWallet(
    val id: String = "",
    val Email: String = "",
    val balance: Double = 100000.0,
    val cryptoHoldings: Map<String, Double> = mapOf()
)