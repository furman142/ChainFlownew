package com.example.chain_flow.utils

object PriceFormatter {
    fun formatPrice(price: Double): String {
        return "$${String.format("%.2f", price)}"
    }
} 