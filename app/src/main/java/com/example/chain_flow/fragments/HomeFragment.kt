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
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: CryptocardAdapter
    private val cryptoList = mutableListOf<CryptoCoin>()
    private lateinit var coinTab: MaterialButton
    private lateinit var watchlistTab: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        initializeViews(view)
        setupRecyclerView()
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
            coinTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            watchlistTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            adapter.showWatchlistOnly(false)
        }

        watchlistTab.setOnClickListener {
            watchlistTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            coinTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            adapter.showWatchlistOnly(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = CryptocardAdapter(
            cryptoList,
            requireContext(),
            onWatchlistChanged = { position, isWatchlisted ->
                cryptoList[position].watchlist = isWatchlisted
                // If we're in watchlist view, refresh the list
                if (watchlistTab.currentTextColor == ContextCompat.getColor(requireContext(), R.color.black)) {
                    adapter.notifyDataSetChanged()
                }
            },
            onCardClicked = { crypto ->
                showBuyFragment(crypto)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun loadCryptoData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getLatestListings()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val cryptoDataList = response.body()?.data ?: emptyList()
                        
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
                    swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun showBuyFragment(crypto: CryptoCoin) {
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
}
