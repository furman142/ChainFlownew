package com.example.chain_flow.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AssetAdapter
    private lateinit var totalBalanceTextView: TextView
    private lateinit var change24hTextView: TextView
    private lateinit var assetCountTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val db = FirebaseFirestore.getInstance()
    private var initialBalance = 10000.0
    private var currentBalance = 10000.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_portfolio, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.assets_recycler_view)
        totalBalanceTextView = view.findViewById(R.id.total_balance)
        change24hTextView = view.findViewById(R.id.change_24h)
        assetCountTextView = view.findViewById(R.id.asset_count)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // Setup RecyclerView
        adapter = AssetAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            fetchWalletData()
        }

        // Load wallet data
        fetchWalletData()

        return view
    }

    private fun fetchWalletData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please log in to view your portfolio", Toast.LENGTH_LONG).show()
            return
        }

        val walletRef = db.collection("users").document(userId).collection("Wallet")
        walletRef.get()
            .addOnSuccessListener { snapshot ->
                val assetList = mutableListOf<Asset>()
                var totalWorth = 0.0

                for (document in snapshot.documents) {
                    val data = document.data
                    if (data != null) {
                        try {
                            val asset = Asset.fromMap(data)
                            assetList.add(asset)
                            totalWorth += asset.worth
                        } catch (e: Exception) {
                            println("Error processing asset: ${e.message}")
                        }
                    }
                }

                adapter.updateAssets(assetList)
                assetCountTextView.text = "${assetList.size} assets"
                totalBalanceTextView.text = "$${String.format("%.2f", totalWorth)}"
                // For now, just show 0% change
                change24hTextView.text = "+$0.00 (0.00%)"
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch wallet: ${exception.message}", 
                    Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        fetchWalletData() // Refresh data when fragment becomes visible
    }
}
