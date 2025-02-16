package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chain_flow.R
import com.example.chain_flow.adapters.CryptocardAdapter
import com.example.chain_flow.api.RetrofitClient
import com.example.chain_flow.models.CryptoCoin
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : BaseFragment() {
    // UI Components
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var coinTab: MaterialButton
    private lateinit var watchlistTab: MaterialButton
    
    private lateinit var adapter: CryptocardAdapter
    private val cryptoList = mutableListOf<CryptoCoin>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        // Initialize views after inflating the layout
        initializeViews(view)
        setupRecyclerView(view)
        setupTabListeners()
        loadCryptoData()
        
        return view
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        coinTab = view.findViewById(R.id.coin_tab)
        watchlistTab = view.findViewById(R.id.watchlist_tab)
        
        swipeRefreshLayout.setOnRefreshListener {
            loadCryptoData()
        }
    }

    private fun setupTabListeners() {
        coinTab.setOnClickListener {
            updateTabSelection(isWatchlistSelected = false)
        }

        watchlistTab.setOnClickListener {
            updateTabSelection(isWatchlistSelected = true)
        }
    }

    private fun updateTabSelection(isWatchlistSelected: Boolean) {
        val context = requireContext()
        coinTab.setTextColor(ContextCompat.getColor(context, 
            if (!isWatchlistSelected) R.color.black else R.color.gray))
        watchlistTab.setTextColor(ContextCompat.getColor(context, 
            if (isWatchlistSelected) R.color.black else R.color.gray))
        adapter.showWatchlistOnly(isWatchlistSelected)
    }

    private fun setupRecyclerView(view: View) {
        adapter = CryptocardAdapter(
            cryptoList,
            requireContext(),
            onWatchlistChanged = { position, isWatchlisted ->
                cryptoList[position].watchlist = isWatchlisted
            },
            onCardClicked = { crypto ->
                navigateToBuyFragment(crypto)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun handleWatchlistChange(position: Int, isWatchlisted: Boolean) {
        cryptoList[position].watchlist = isWatchlisted
        if (watchlistTab.currentTextColor == 
            ContextCompat.getColor(requireContext(), R.color.black)) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun navigateToBuyFragment(crypto: CryptoCoin) {
        val buyFragment = BuyFragment().apply {
            arguments = Bundle().apply {
                putString("cryptoName", crypto.cryptoName)
                putString("cryptoValue", crypto.cryptoValue)
                putString("imageUrl", crypto.imageUrl)
                putDouble("rawPrice", crypto.rawPrice)
            }
        }
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, buyFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadCryptoData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getLatestListings()
                
                withContext(Dispatchers.Main) {
                    handleCryptoResponse(response)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun handleCryptoResponse(response: retrofit2.Response<com.example.chain_flow.api.CryptoListResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val cryptoDataList = response.body()?.data ?: emptyList()
            updateCryptoList(cryptoDataList)
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun updateCryptoList(cryptoDataList: List<com.example.chain_flow.api.CryptoData>) {
        cryptoList.clear()
        cryptoList.addAll(cryptoDataList.map { data ->
            CryptoCoin(
                cryptoName = data.name,
                cryptoValue = "$${String.format("%.2f", data.quote.USD.price)}",
                imageUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/${data.id}.png",
                watchlist = false,
                description = "${data.name} (${data.symbol})",
                rawPrice = data.quote.USD.price
            )
        })
        adapter.notifyDataSetChanged()
    }

    private suspend fun handleError(e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "שגיאה: ${e.message}", Toast.LENGTH_LONG).show()
            swipeRefreshLayout.isRefreshing = false
        }
    }
}
