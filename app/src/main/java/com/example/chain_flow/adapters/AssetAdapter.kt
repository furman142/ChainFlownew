package com.example.chain_flow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chain_flow.R
import com.example.chain_flow.models.Asset

class AssetAdapter(
    private var assets: List<Asset>
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cryptoIcon: ImageView = itemView.findViewById(R.id.crypto_icon)
        private val cryptoName: TextView = itemView.findViewById(R.id.crypto_name)
        private val cryptoAmount: TextView = itemView.findViewById(R.id.crypto_amount)
        private val cryptoWorth: TextView = itemView.findViewById(R.id.crypto_worth)

        fun bind(asset: Asset) {
            cryptoName.text = "${asset.cryptoName} (${asset.symbol})"
            cryptoAmount.text = String.format("%.8f", asset.amount)
            cryptoWorth.text = String.format("$%.2f", asset.worth)

            // Load crypto icon with Glide
            Glide.with(itemView.context)
                .load(asset.imageUrl)
                .placeholder(R.drawable.crypto_placeholder)
                .error(R.drawable.crypto_placeholder)
                .circleCrop()
                .into(cryptoIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(assets[position])
    }

    override fun getItemCount() = assets.size

    fun updateAssets(newAssets: List<Asset>) {
        assets = newAssets
        notifyDataSetChanged()
    }
}
