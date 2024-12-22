package com.example.chain_flow.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chain_flow.R
import com.example.chain_flow.models.CryptoCoin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CryptocardAdapter(
    private var cryptoList: List<CryptoCoin>,
    private val context: Context,
    private val onWatchlistChanged: (Int, Boolean) -> Unit,
    private val onCardClicked: (CryptoCoin) -> Unit
) : RecyclerView.Adapter<CryptocardAdapter.CryptoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cryptoName: TextView = itemView.findViewById(R.id.crypto_name)
        val cryptoValue: TextView = itemView.findViewById(R.id.crypto_price)
        val watchlistButton: ImageView = itemView.findViewById(R.id.watchlist_button)
        val cryptoIcon: ImageView = itemView.findViewById(R.id.crypto_icon)

        fun bind(crypto: CryptoCoin, position: Int) {
            // לוקח url  מוריד אותו לוקלית למחשב ומראה את התמונה בכרטיס הנוכחי coinmarketcap
            Glide.with(context)
                .load(crypto.imageUrl)
                // עד שהתמונה עולה מה המשתמש רואה
                .placeholder(R.drawable.bitcoin)
                .error(R.drawable.bitcoin)
                //לאן אתה מזריק את התמונה
                .into(cryptoIcon)

            cryptoName.text = crypto.cryptoName
            cryptoValue.text = crypto.cryptoValue
            
            // Set initial state of watchlist button
            watchlistButton.isSelected = crypto.watchlist
            
            // Make sure the button is clickable
            watchlistButton.isClickable = true
            watchlistButton.isFocusable = true
            
            // Check if crypto is in watchlist
            auth.currentUser?.let { user ->
                db.collection("users")
                    .document(user.uid)
                    .collection("watchlist")
                    .document(crypto.cryptoName)
                    .get()
                    .addOnSuccessListener { document ->
                        val isWatchlisted = document != null && document.exists()
                        watchlistButton.isSelected = isWatchlisted
                        crypto.watchlist = isWatchlisted
                    }
            }

            // אלגוריתם המעומדים לצפייה
            watchlistButton.setOnClickListener {
                auth.currentUser?.let { user ->
                    val newState = !crypto.watchlist
                    val cryptoRef = db.collection("users")
                        .document(user.uid)
                        .collection("watchlist")
                        .document(crypto.cryptoName)

                    // Show immediate feedback
                    watchlistButton.isSelected = newState
                    
                    if (newState) {
                        // Add to watchlist
                        val watchlistData = hashMapOf(
                            "cryptoName" to crypto.cryptoName,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        cryptoRef.set(watchlistData)
                            .addOnSuccessListener {
                                crypto.watchlist = true
                                onWatchlistChanged(position, true)
                                Toast.makeText(context, "Added to watchlist", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                // Revert on failure
                                watchlistButton.isSelected = false
                                crypto.watchlist = false
                                Toast.makeText(context, "Failed to add to watchlist", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Remove from watchlist
                        cryptoRef.delete()
                            .addOnSuccessListener {
                                crypto.watchlist = false
                                onWatchlistChanged(position, false)
                                Toast.makeText(context, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                // Revert on failure
                                watchlistButton.isSelected = true
                                crypto.watchlist = true
                                Toast.makeText(context, "Failed to remove from watchlist", Toast.LENGTH_SHORT).show()
                            }
                    }
                } ?: Toast.makeText(context, "Please login to use watchlist", Toast.LENGTH_SHORT).show()
            }

            // מה קורה שהמשתמש לוחץ
            itemView.setOnClickListener {
                onCardClicked(crypto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cryptocard, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val filteredList = if (showWatchlistOnly) {
            cryptoList.filter { it.watchlist }
        } else {
            cryptoList
        }
        
        if (position < filteredList.size) {
            holder.bind(filteredList[position], cryptoList.indexOf(filteredList[position]))
        }
    }

    override fun getItemCount(): Int {
        return if (showWatchlistOnly) {
            cryptoList.filter { it.watchlist }.size
        } else {
            cryptoList.size
        }
    }

    private var showWatchlistOnly = false

    fun showWatchlistOnly(show: Boolean) {
        showWatchlistOnly = show
        notifyDataSetChanged()
    }

    fun updateCryptoList(newList: List<CryptoCoin>) {
        cryptoList = newList
        notifyDataSetChanged()
    }
}
