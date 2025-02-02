// חבילת הקוד - מאורגן לפי נושא (כאן, קוד הקשור לחיבורי API)
package com.example.chain_flow.api

// ייבוא מחלקות הדרושות ל-Retrofit ו-OkHttp
import com.example.chain_flow.BuildConfig // גישה למפתחות API מתוך ה-build.gradle
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * מחלקה אובייקטית (Singleton) ליצירת החיבור ל-API
 */
object RetrofitClient {
    // **כתובת הבסיס של ה-API**
    private const val BASE_URL = "https://pro-api.coinmarketcap.com/"

    // **Interceptor**: מאפשר לראות לוגים של הקריאות ל-API (שימושי לדיבאגינג)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // קובע את רמת הפירוט של הלוגים (BODY = הכי מפורט)
        level = HttpLoggingInterceptor.Level.BODY
    }

    // **OkHttpClient**: מגדיר את הלקוח שאיתו נעבוד (כולל כותרות ובקרת לוגים)
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request() // הקריאה המקורית
            // מוסיף כותרת (Header) עם מפתח ה-API לכל קריאה
            val request = original.newBuilder()
                .header("X-CMC_PRO_API_KEY", BuildConfig.CMC_API_KEY) // המפתח מאובטח בקובץ build.gradle
                .method(original.method, original.body) // משאיר את השיטה והגוף המקוריים
                .build()
            chain.proceed(request) // מבצע את הקריאה עם השינויים שהוספנו
        }
        .addInterceptor(loggingInterceptor) // מוסיף את ה-logging interceptor
        .build()

    // **Retrofit Instance**: יוצר מופע של Retrofit עם ההגדרות הדרושות
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // מגדיר את כתובת הבסיס של ה-API
        .client(client) // משתמש ב-OkHttpClient שהגדרנו
        .addConverterFactory(GsonConverterFactory.create()) // ממיר JSON לאובייקטים של Kotlin
        .build()

    // **API Interface**: יוצרים מופע של ה-API שנשתמש בו בקוד
    val api: CoinMarketCapApi = retrofit.create(CoinMarketCapApi::class.java)
}
