package com.example.chain_flow.models

import com.example.chain_flow.R

data class CryptoCoin(
    val cryptoName: String,
    val cryptoValue: String,
    val imageUrl: Int = R.drawable.ic_bnb,
    var watchlist: Boolean = false,
    var description: String
)
