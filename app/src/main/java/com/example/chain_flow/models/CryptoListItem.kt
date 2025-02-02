package com.example.chain_flow.models

data class CryptoListItem(
    val name: String,
    val symbol: String,
    val price: Double,
    val id: Int
) {
    override fun toString(): String {
        return "$name ($symbol)"
    }
} 