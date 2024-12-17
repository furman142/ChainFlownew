import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chain_flow.R
import com.example.chain_flow.models.CryptoCoin
import com.example.chain_flow.networks.CoinMarketApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SecondActivity : AppCompatActivity() {
    private lateinit var cryptoTextView: TextView

    companion object {
        private const val BASE_URL = "https://pro-api.coinmarketcap.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Initialize TextView
        cryptoTextView = findViewById(R.id.cryptoTextView)

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create API service
        val coinMarketCapService = retrofit.create(CoinMarketCapApi::class.java)

        // Make API call
        val call = coinMarketCapService.getAllCryptos()
        call.enqueue(object : Callback<List<CryptoCoin>> {
            override fun onResponse(call: Call<List<CryptoCoin>>, response: Response<List<CryptoCoin>>) {
                if (response.isSuccessful) {
                    val cryptoList = response.body()
                    val cryptoDisplayText = StringBuilder()

                    cryptoList?.take(10)?.forEach { crypto ->
                        cryptoDisplayText.append("Name: ${crypto.name}\n")
                        cryptoDisplayText.append("Price: $${String.format("%.2f", crypto.price)}\n\n")
                    }

                    // Update UI on main thread
                    runOnUiThread {
                        cryptoTextView.text = cryptoDisplayText.toString()
                    }
                } else {
                    // Handle error
                    runOnUiThread {
                        cryptoTextView.text = "Error: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<List<CryptoCoin>>, t: Throwable) {
                // Handle network error
                runOnUiThread {
                    cryptoTextView.text = "Network Error: ${t.message}"
                }
            }
        })
    }
}