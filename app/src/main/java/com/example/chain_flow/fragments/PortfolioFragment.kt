package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chain_flow.R

class PortfolioFragment : Fragment() {
    private lateinit var totalBalanceTextView: TextView
    private lateinit var assetsRecyclerView: RecyclerView
    private lateinit var cryptoSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_portfolio, container, false)

        // Initialize views
        totalBalanceTextView = view.findViewById(R.id.total_balance)
        assetsRecyclerView = view.findViewById(R.id.assets_recycler_view)
        cryptoSpinner = view.findViewById(R.id.crypto_spinner)

        // Setup RecyclerView
        assetsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Setup Spinner
        setupCryptoSpinner()

        return view
    }

    private fun setupCryptoSpinner() {
        val cryptoList = arrayOf(
            "Select Cryptocurrency",  // Default first item
            "Bitcoin (BTC)",
            "Ethereum (ETH)",
            "Binance Coin (BNB)",
            "Cardano (ADA)",
            "Solana (SOL)",
            "XRP (XRP)",
            "Polkadot (DOT)",
            "Dogecoin (DOGE)",
            "Avalanche (AVAX)",
            "Chainlink (LINK)"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cryptoList
        )
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cryptoSpinner.adapter = adapter
    }
}
