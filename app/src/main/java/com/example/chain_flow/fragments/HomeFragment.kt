import PortfolioFragment
import TradeFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chain_flow.models.CryptoCoin
import com.example.chain_flow.R
import com.example.chain_flow.adapters.CryptocardAdapter
import com.example.chain_flow.fragments.MarketFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bumptech.glide.Glide
import android.widget.Toast
import com.example.chain_flow.api.RetrofitClient

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CryptocardAdapter
    private val cryptoList = arrayListOf<CryptoCoin>()

    // Detailed view elements
    private lateinit var cardDetailsLayout: View
    private lateinit var backButton: ImageButton
    private lateinit var cryptoNameTextView: TextView
    private lateinit var cryptoPriceTextView: TextView
    private lateinit var cryptoDescriptionTextView: TextView

    // Tab and navigation buttons
    private lateinit var coinTab: MaterialButton
    private lateinit var watchlistTab: MaterialButton
    private lateinit var marketsButton: MaterialButton
    private lateinit var tradeButton: MaterialButton
    private lateinit var portfolioButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupRecyclerView()
        setupTabListeners()
        setupBottomNavigation()

        return view
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        cardDetailsLayout = view.findViewById(R.id.card_details_layout)
        backButton = view.findViewById(R.id.back_button)
        cryptoNameTextView = view.findViewById(R.id.crypto_name)
        cryptoPriceTextView = view.findViewById(R.id.crypto_price)
        cryptoDescriptionTextView = view.findViewById(R.id.crypto_description)

        coinTab = view.findViewById(R.id.coin_tab)
        watchlistTab = view.findViewById(R.id.watchlist_tab)
        marketsButton = view.findViewById(R.id.markets_button)
        tradeButton = view.findViewById(R.id.trade_button)
        portfolioButton = view.findViewById(R.id.portfolio_button)

        // Handle back button click
        backButton.setOnClickListener {
            showRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CryptocardAdapter(
            cryptoList,
            requireContext(),
            onWatchlistChanged = { position, isChecked ->
                cryptoList[position].watchlist = isChecked
            },
            onCardClicked = { cryptoCoin ->
                showCardDetails(cryptoCoin)
            }
        )
        recyclerView.adapter = adapter
        
        // Load data from API
        loadCryptoData()
    }

    private fun loadCryptoData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getLatestListings()
                if (response.isSuccessful) {
                    val cryptoDataList = response.body()?.data ?: emptyList()
                    
                    val cryptoCoins = cryptoDataList.map { data ->
                        CryptoCoin(
                            cryptoName = data.name,
                            cryptoValue = "$${String.format("%.2f", data.quote.USD.price)}",
                            imageUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/${data.id}.png",
                            watchlist = false,
                            description = "Digital currency ${data.name} (${data.symbol})"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        cryptoList.clear()
                        cryptoList.addAll(cryptoCoins)
                        adapter.notifyDataSetChanged()
                        println("Loaded ${cryptoList.size} coins")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupTabListeners() {
        coinTab.setOnClickListener {
            updateTabSelection(true)
            adapter.showWatchlistOnly(false)
        }

        watchlistTab.setOnClickListener {
            updateTabSelection(false)
            adapter.showWatchlistOnly(true)
        }
    }

    private fun updateTabSelection(coinTabSelected: Boolean) {
        coinTab.setTextColor(resources.getColor(if (coinTabSelected) R.color.black else R.color.black, null))
        watchlistTab.setTextColor(resources.getColor(if (coinTabSelected) R.color.black else R.color.black, null))
    }

    private fun setupBottomNavigation() {
        marketsButton.setOnClickListener {
            val marketFragment= MarketFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,marketFragment)
                .addToBackStack(null)
                .commit()
        }

        tradeButton.setOnClickListener {
            val tradeFragment = TradeFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, tradeFragment)
                .addToBackStack(null)
                .commit()
        }

        portfolioButton.setOnClickListener {
            val portfolioFragment=PortfolioFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,portfolioFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showCardDetails(cryptoCoin: CryptoCoin) {
        recyclerView.visibility = View.GONE
        cardDetailsLayout.visibility = View.VISIBLE

        // Load detailed image
        val detailImage = view?.findViewById<ImageView>(R.id.detail_crypto_image)
        detailImage?.let {
            Glide.with(requireContext())
                .load(cryptoCoin.imageUrl)
                .placeholder(R.drawable.bitcoin)
                .error(R.drawable.bitcoin)
                .into(it)
        }

        cryptoNameTextView.text = cryptoCoin.cryptoName
        cryptoPriceTextView.text = "$${cryptoCoin.cryptoValue}"
        cryptoDescriptionTextView.text = cryptoCoin.description
    }

    private fun showRecyclerView() {
        // Hide the detailed view and show the RecyclerView
        cardDetailsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}