package com.example.chain_flow.data.repository

import com.example.chain_flow.api.CoinMarketCapApi
import com.google.firebase.firestore.FirebaseFirestore

class CryptoRepository(
    private val api: CoinMarketCapApi,
    private val db: FirebaseFirestore
) {
    suspend fun getLatestCryptos() = api.getLatestListings()
    
    fun getUserWatchlist(userId: String) = db.collection("users")
        .document(userId)
        .collection("watchlist")
        .get()
} 