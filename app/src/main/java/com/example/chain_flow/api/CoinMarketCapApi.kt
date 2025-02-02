// חבילה שמארגנת את הקוד לפי נושא (כאן מדובר בקוד שקשור לתקשורת עם API)
package com.example.chain_flow.api

// ייבוא של מחלקות וספריות הנדרשות ל-Retrofit
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// הגדרת ממשק API - זה החוזה שמגדיר את הקריאות שניתן לבצע ל-CoinMarketCap
interface CoinMarketCapApi {

    // קריאה מסוג GET לנתיב "v1/cryptocurrency/listings/latest"
    @GET("v1/cryptocurrency/listings/latest")
    suspend fun getLatestListings(
        // פרמטר שמגדיר מאיפה להתחיל את הרשימה (למשל, החל ממטבע מס' 1)
        @Query("start") start: Int = 1,
        // פרמטר שמגדיר כמה מטבעות להחזיר בתגובה (ברירת מחדל: 100)
        @Query("limit") limit: Int = 100,
        // פרמטר שמגדיר את המטבע שבו תבוצע ההמרה (ברירת מחדל: USD)
        @Query("convert") convert: String = "USD"
    ): Response<CryptoListResponse> // מחזיר תגובה עם הנתונים שנשלפו מה-API
}

// מחלקה שמייצגת את התגובה מה-API (שכבת המידע הראשית)
data class CryptoListResponse(
    val data: List<CryptoData> // רשימה של נתוני מטבעות קריפטו
)

// מחלקה שמייצגת מטבע קריפטו בודד (נתון ספציפי בתוך הרשימה)
data class CryptoData(
    val id: Int,        // מזהה המטבע
    val name: String,   // שם המטבע (למשל, "Bitcoin")
    val symbol: String, // הסמל של המטבע (למשל, "BTC")
    val quote: Quote    // מידע נוסף על המטבע
)

// מחלקה שמייצגת מידע נוסף על המטבע (מחירים, שינויים וכו')
data class Quote(
    val USD: UsdData // המידע בשקלים אמריקאיים
)

// מחלקה שמייצגת את המידע בשקלים אמריקאיים
data class UsdData(
    val price: Double,               // מחיר המטבע הנוכחי
    val percent_change_24h: Double   // אחוז השינוי ב-24 השעות האחרונות
)
