package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chain_flow.R

class PortfolioFragment : Fragment() {
    private lateinit var totalBalanceTextView: TextView
    private lateinit var change24hTextView: TextView
    private lateinit var assetCountTextView: TextView
    private lateinit var assetsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_portfolio, container, false)

        // Initialize views
        totalBalanceTextView = view.findViewById(R.id.total_balance)
        change24hTextView = view.findViewById(R.id.change_24h)
        assetCountTextView = view.findViewById(R.id.asset_count)
        assetsRecyclerView = view.findViewById(R.id.assets_recycler_view)

        // Setup RecyclerView
        assetsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Update UI
        updatePortfolioData()

        return view
    }

    private fun updatePortfolioData() {
        // TODO: Replace with real data
        totalBalanceTextView.text = "$1,234.56"
        change24hTextView.text = "+$123.45 (10.00%)"
        assetCountTextView.text = "3 assets"
    }
}
