package com.example.chain_flow.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chain_flow.R
import com.example.chain_flow.models.CryptoCoin

class CryptocardAdapter(
    private var cryptoList: List<CryptoCoin>,
    private val context: Context,
    private val onWatchlistChanged: (Int, Boolean) -> Unit,
    private val onCardClicked: (CryptoCoin) -> Unit
) : RecyclerView.Adapter<CryptocardAdapter.CryptoViewHolder>() {

    private var showWatchlistOnly = false

    class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cryptoName: TextView = itemView.findViewById(R.id.crypto_name)
        val cryptoValue: TextView = itemView.findViewById(R.id.crypto_price)
        val watchlistButton: ImageView = itemView.findViewById(R.id.watchlist_button)
        val cryptoIcon: ImageView = itemView.findViewById(R.id.crypto_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cryptocard, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = cryptoList[position]

        holder.cryptoName.text = crypto.cryptoName
        holder.cryptoValue.text = crypto.cryptoValue
        holder.cryptoIcon.setImageResource(crypto.imageUrl)
        
        holder.watchlistButton.isSelected = crypto.watchlist
        holder.watchlistButton.setOnClickListener {
            val newState = !crypto.watchlist
            onWatchlistChanged(position, newState)
            holder.watchlistButton.isSelected = newState
        }

        holder.itemView.setOnClickListener {
            onCardClicked(crypto)
        }
    }

    override fun getItemCount(): Int {
        return if (showWatchlistOnly) {
            cryptoList.filter { it.watchlist }.size
        } else {
            cryptoList.size
        }
    }

    fun showWatchlistOnly(show: Boolean) {
        showWatchlistOnly = show
        notifyDataSetChanged()
    }
}
