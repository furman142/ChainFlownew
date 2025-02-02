package com.example.chain_flow.models

data class Asset(
    val cryptoName: String,
    val symbol: String,
    val amount: Double,
    val worth: Double,
    val imageUrl: String,
    val buyPrice: Double,
    val timestamp: com.google.firebase.Timestamp
) {
    companion object {
        fun fromMap(data: Map<String, Any>): Asset {
            val cryptoName = data["cryptoName"] as String
            // Map crypto names to their CoinMarketCap IDs
            val cryptoIds = mapOf(
                "Bitcoin" to 1,
                "Ethereum" to 1027,
                "Binance Coin" to 1839,
                "Cardano" to 2010,
                "Solana" to 5426,
                "Ripple" to 52,
                "Dogecoin" to 74,
                "Polkadot" to 6636,
                "Polygon" to 3890,
                "Shiba Inu" to 5994
            )
            
            // Get the correct ID for the crypto, default to Bitcoin if not found
            val cryptoId = cryptoIds[cryptoName] ?: 1
            
            // Generate the correct image URL using the crypto ID
            val imageUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/$cryptoId.png"

            return Asset(
                cryptoName = cryptoName,
                symbol = when(cryptoName) {
                    "Bitcoin" -> "BTC"
                    "Ethereum" -> "ETH"
                    "Binance Coin" -> "BNB"
                    "Cardano" -> "ADA"
                    "Solana" -> "SOL"
                    "Ripple" -> "XRP"
                    "Dogecoin" -> "DOGE"
                    "Polkadot" -> "DOT"
                    "Polygon" -> "MATIC"
                    "Shiba Inu" -> "SHIB"
                    else -> "BTC"
                },
                amount = data["amount"] as Double,
                worth = data["worth"] as Double,
                imageUrl = imageUrl,
                buyPrice = data["buyPrice"] as Double,
                timestamp = data["timestamp"] as com.google.firebase.Timestamp
            )
        }
    }
} 