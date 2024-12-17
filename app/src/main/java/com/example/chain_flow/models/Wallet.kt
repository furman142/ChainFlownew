package com.example.chain_flow.models

data class Wallet(
    val id: String = "",
    val userId: String = "",
    val balance: Double = 0.0,
    val cryptoHoldings: Map<String, Double> = mapOf()
) {
    // Secondary constructor for creating an empty wallet
    constructor(userId: String) : this(
        id = java.util.UUID.randomUUID().toString(),
        userId = userId,
        balance = 0.0,
        cryptoHoldings = emptyMap()
    )
}
