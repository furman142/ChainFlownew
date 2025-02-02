package com.example.chain_flow.api.models

import com.example.chain_flow.api.Quote
import com.example.chain_flow.models.Status

data class CryptoResponse(
    val data: List<CryptoData>,
    val status: Status
)

data class CryptoData(
    val id: Int,
    val name: String,
    val symbol: String,
    val quote: Quote
)

// ... rest of API models 