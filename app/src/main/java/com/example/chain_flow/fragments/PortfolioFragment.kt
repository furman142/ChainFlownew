import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.chain_flow.R
import kotlin.properties.Delegates

class PortfolioFragment : Fragment() {
    private val initialBalance = 10000.0 // Set initial currency to 10,000



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_portfolio, container, false)

        var totalBalanceTextView: TextView = view.findViewById(R.id.total_balance)
                totalBalanceTextView.text = "$10,000.00"

       return  view
    }
} 