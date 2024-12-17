import com.example.chain_flow.models.CryptoCoin
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface CoinMarketCapApi {
    @GET("v1/cryptocurrency/listings/latest")
    fun getAllCryptos(
        @Header("1650ec97-b3dd-4c71-abe5-a1575664bb6d") apiKey: String
    ): Call<List<CryptoCoin>>
}