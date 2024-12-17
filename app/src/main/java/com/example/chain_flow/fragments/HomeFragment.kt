
import PortfolioFragment
import TradeFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chain_flow.models.CryptoCoin
import com.example.chain_flow.R
import com.example.chain_flow.adapters.CryptocardAdapter
import com.example.chain_flow.fragments.MarketFragment
import com.google.android.material.button.MaterialButton

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

        cryptoList.apply {
            add(CryptoCoin("Bitcoin", "95000", R.drawable.bitcoin, false, "Bitcoin is the first decentralized cryptocurrency, enabling peer-to-peer transactions on a global scale."))
            add(CryptoCoin("Ethereum", "3000", R.drawable.eth, false, "Ethereum is a decentralized platform that supports smart contracts and decentralized applications."))
            add(CryptoCoin("BNB", "45000", R.drawable.bnb, false, "BNB is the native cryptocurrency of the Binance exchange, used for trading fees and other utilities."))
            add(CryptoCoin("Solana", "180", R.drawable.sol, false, "Solana is a high-performance blockchain supporting decentralized apps and crypto projects."))
            add(CryptoCoin("Cardano", "0.65", R.drawable.ada, false, "Cardano is a blockchain platform for changemakers, innovators, and visionaries."))
            add(CryptoCoin("XRP", "0.75", R.drawable.xrp, false, "XRP is a digital payment protocol designed to enable fast and affordable cross-border transactions."))
            add(CryptoCoin("Polkadot", "8.50", R.drawable.dot, false, "Polkadot enables blockchain interoperability, allowing diverse blockchains to transfer messages and value."))
            add(CryptoCoin("Dogecoin", "0.15", R.drawable.doge, false, "Dogecoin started as a joke cryptocurrency but has grown to have a loyal community and real-world use cases."))
            add(CryptoCoin("Avalanche", "40.25", R.drawable.avax, false, "Avalanche is a decentralized platform for launching highly scalable DeFi applications."))
            add(CryptoCoin("Chainlink", "18.90", R.drawable.link, false, "Chainlink is a decentralized oracle network providing tamper-proof data for smart contracts."))
            add(CryptoCoin("Polygon", "0.95", R.drawable.matic, false, "Polygon is a Layer 2 scaling solution that aims to improve transaction speed and reduce costs on Ethereum."))
            add(CryptoCoin("Uniswap", "7.80", R.drawable.uni, false, "Uniswap is a decentralized exchange protocol built on Ethereum for swapping ERC-20 tokens."))
            add(CryptoCoin("Litecoin", "80.50", R.drawable.ltc, false, "Litecoin is a peer-to-peer cryptocurrency designed for fast and inexpensive transactions."))
        }


        // Set up the adapter
        adapter = CryptocardAdapter(
            cryptoList,
            requireContext(),
            onWatchlistChanged = { position, isChecked ->
                // Update watchlist status
                cryptoList[position].watchlist = isChecked
            },
            onCardClicked = { cryptoCoin ->
                // Show detailed view
                showCardDetails(cryptoCoin)
            }
        )
        recyclerView.adapter = adapter
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
        // Hide RecyclerView and show the detailed view
        recyclerView.visibility = View.GONE
        cardDetailsLayout.visibility = View.VISIBLE

        // Populate the detailed view with crypto data
        cryptoNameTextView.text = cryptoCoin.cryptoName
        cryptoPriceTextView.text = "$${cryptoCoin.cryptoValue}"
        cryptoDescriptionTextView.text = cryptoCoin.description // Add description logic here
    }

    private fun showRecyclerView() {
        // Hide the detailed view and show the RecyclerView
        cardDetailsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}