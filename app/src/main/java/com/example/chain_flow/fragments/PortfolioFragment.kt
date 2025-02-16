package com.example.chain_flow.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chain_flow.R
import com.example.chain_flow.adapters.AssetAdapter
import com.example.chain_flow.api.RetrofitClient
import com.example.chain_flow.models.Asset
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortfolioFragment : BaseFragment() {
    // UI Components
    private lateinit var totalBalanceView: TextView
    private lateinit var assetCountView: TextView
    private lateinit var adapter: AssetAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transferButton: Button

    // Firebase instances
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_portfolio, container, false)
        
        initializeViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        
        // Load data immediately
        fetchUserData()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserData() // Refresh data when fragment becomes visible
    }

    private fun initializeViews(view: View) {
        totalBalanceView = view.findViewById(R.id.total_balance)
        assetCountView = view.findViewById(R.id.asset_count)
        recyclerView = view.findViewById(R.id.assets_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        transferButton = view.findViewById(R.id.transfer)

        view.findViewById<Button>(R.id.transfer).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TransferFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserData()
        }
    }

    private fun setupRecyclerView() {
        adapter = AssetAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun fetchUserData() {
        val user = auth.currentUser ?: run {
            showToast("יש להתחבר תחילה")
            swipeRefreshLayout.isRefreshing = false
            return
        }

        // Get user's balance first
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    // If user document doesn't exist, create it with initial balance
                    val userData = hashMapOf(
                        "email" to user.email,
                        "balance" to 1000000000.0, // 1B USD
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            fetchWalletData(user.uid, 1000000000.0)
                        }
                        .addOnFailureListener { e ->
                            showToast("שגיאה ביצירת משתמש: ${e.message}")
                            swipeRefreshLayout.isRefreshing = false
                        }
                } else {
                    // User exists, get their balance and DON'T reset it
                    val balance = userDoc.getDouble("balance") ?: 0.0  // Changed from 1000000000.0 to 0.0
                    fetchWalletData(user.uid, balance)
                }
            }
            .addOnFailureListener { e ->
                showToast("שגיאה בטעינת נתוני משתמש: ${e.message}")
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun fetchWalletData(userId: String, balance: Double) {
        // Get current crypto prices
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getLatestListings()
                if (response.isSuccessful && response.body() != null) {
                    val currentPrices = response.body()?.data?.associate { 
                        it.name to it.quote.USD.price 
                    } ?: emptyMap()

                    withContext(Dispatchers.Main) {
                        loadWalletAssets(userId, balance, currentPrices)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Even if we can't get prices, still show the balance
                    updateTotalBalance(balance)
                    showToast("שגיאה בטעינת מחירים: ${e.message}")
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun loadWalletAssets(userId: String, balance: Double, currentPrices: Map<String, Double>) {
        db.collection("users").document(userId)
            .collection("wallet")
            .get()
            .addOnSuccessListener { documents ->
                var totalAssetsWorth = 0.0
                val assets = documents.mapNotNull { doc ->
                    try {
                        val cryptoName = doc.getString("cryptoName") ?: return@mapNotNull null
                        val amount = doc.getDouble("amount") ?: 0.0
                        val buyPrice = doc.getDouble("buyPrice") ?: 0.0
                        
                        val currentPrice = currentPrices[cryptoName] ?: buyPrice
                        val worth = amount * currentPrice
                        
                        totalAssetsWorth += worth

                        Asset(
                            cryptoName = cryptoName,
                            symbol = getCryptoSymbol(cryptoName),
                            amount = amount,
                            worth = worth,
                            imageUrl = getCryptoImageUrl(cryptoName),
                            buyPrice = buyPrice,
                            timestamp = doc.getTimestamp("timestamp") 
                                ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Update UI with total (balance + assets worth)
                val totalWorth = balance + totalAssetsWorth  // Use the actual balance from Firestore
                updateTotalBalance(totalWorth)
                adapter.updateAssets(assets)
                assetCountView.text = "${assets.size} assets"
                swipeRefreshLayout.isRefreshing = false

                // Update assets worth in Firestore without changing balance
                updateAssetsWorthInFirestore(userId, assets)
            }
            .addOnFailureListener { e ->
                updateTotalBalance(balance)  // Show the actual balance even if assets fail to load
                showToast("שגיאה בטעינת הארנק: ${e.message}")
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun updateAssetsWorthInFirestore(userId: String, assets: List<Asset>) {
        assets.forEach { asset ->
            db.collection("users").document(userId)
                .collection("wallet")
                .whereEqualTo("cryptoName", asset.cryptoName)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        documents.documents[0].reference.update(
                            mapOf(
                                "worth" to asset.worth,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )
                        )
                    }
                }
        }
    }

    private fun getCryptoImageUrl(name: String): String {
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
            "Shiba Inu" to 5994,
            "Tether" to 825,
            "USD Coin" to 3408,
            "XRP" to 52,
            "Litecoin" to 2,
            "Chainlink" to 1975,
            "Stellar" to 512,
            "Bitcoin Cash" to 1831,
            "Monero" to 328,
            "Cosmos" to 3794,
            "Tron" to 1958
        )
        
        return when {
            cryptoIds.containsKey(name) -> {
                val id = cryptoIds[name]
                "https://s2.coinmarketcap.com/static/img/coins/64x64/$id.png"
            }
            else -> {
                // אם אין לנו ID ספציפי, ננסה להשתמש בסמל המטבע
                val symbol = getCryptoSymbol(name).toLowerCase()
                "https://s2.coinmarketcap.com/static/img/coins/64x64/$symbol.png"
            }
        }
    }

    private fun getCryptoSymbol(name: String): String {
        return when(name) {
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
            "Tether" -> "USDT"
            "USD Coin" -> "USDC"
            "XRP" -> "XRP"
            "Litecoin" -> "LTC"
            "Chainlink" -> "LINK"
            "Stellar" -> "XLM"
            "Bitcoin Cash" -> "BCH"
            "Monero" -> "XMR"
            "Cosmos" -> "ATOM"
            "Tron" -> "TRX"
            else -> name.take(4).toUpperCase()
        }
    }

    private fun updateTotalBalance(totalWorth: Double) {
        totalBalanceView.text = String.format("$%.2f", totalWorth)
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
