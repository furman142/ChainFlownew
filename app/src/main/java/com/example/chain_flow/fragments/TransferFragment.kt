package com.example.chain_flow.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.chain_flow.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.util.Log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.chain_flow.api.RetrofitClient
import com.example.chain_flow.models.CryptoListItem


class TransferFragment : BaseFragment() {
    // UI Components
    private lateinit var userUidText: TextView
    private lateinit var copyUidButton: Button
    private lateinit var transferButton: Button
    private lateinit var recipientUidInput: EditText
    private lateinit var cryptoSelector: AutoCompleteTextView
    private lateinit var amountInput: EditText
    private val cryptoList = mutableListOf<CryptoListItem>()

    // Firebase instances
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transfer, container, false)
        initializeViews(view)
        setupListeners()
        loadUserCryptos()
        return view
    }

    private fun initializeViews(view: View) {
        userUidText = view.findViewById(R.id.user_uid_text)
        copyUidButton = view.findViewById(R.id.copy_uid_button)
        transferButton = view.findViewById(R.id.transfer_button)
        recipientUidInput = view.findViewById(R.id.recipient_uid_input)
        cryptoSelector = view.findViewById(R.id.crypto_selector)
        amountInput = view.findViewById(R.id.amount_input)

        // Display current user's UID
        auth.currentUser?.let { user ->
            userUidText.text = user.uid
        }

        // Setup crypto selector adapter
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            cryptoList
        )
        cryptoSelector.setAdapter(adapter)
    }

    private fun setupListeners() {
        copyUidButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("User UID", userUidText.text)
            clipboard.setPrimaryClip(clip)
            showToast("UID הועתק ללוח")
        }

        transferButton.setOnClickListener {
            handleTransfer()
        }
    }

    private fun handleTransfer() {
        val selectedCryptoFull = cryptoSelector.text.toString()
        val selectedCrypto = selectedCryptoFull.split(" (")[0]
        val amountToTransfer = amountInput.text.toString().toDoubleOrNull()
        val recipientUid = recipientUidInput.text.toString()

        if (recipientUid.isEmpty() || amountToTransfer == null || selectedCryptoFull.isEmpty()) {
            showToast("נא למלא את כל השדות")
            return
        }

        val currentUser = auth.currentUser ?: run {
            showToast("יש להתחבר תחילה")
            return
        }

        if (currentUser.uid == recipientUid) {
            showToast("לא ניתן להעביר לעצמך")
            return
        }

        // Check sender's wallet first
        db.collection("users")
            .document(currentUser.uid)
            .collection("wallet")
            .whereEqualTo("cryptoName", selectedCrypto)
            .get()
            .addOnSuccessListener { senderWallet ->
                if (senderWallet.isEmpty) {
                    showToast("אין ברשותך את המטבע הזה")
                    return@addOnSuccessListener
                }

                val senderAsset = senderWallet.documents[0]
                val currentAmount = senderAsset.getDouble("amount") ?: 0.0
                val currentPrice = senderAsset.getDouble("buyPrice") ?: 0.0
                val cryptoAmountToTransfer = amountToTransfer / currentPrice

                if (currentAmount < cryptoAmountToTransfer) {
                    showToast("אין מספיק יתרה")
                    return@addOnSuccessListener
                }

                // Now check recipient and perform transfer
                db.collection("users").document(recipientUid)
                    .get()
                    .addOnSuccessListener { recipientDoc ->
                        if (!recipientDoc.exists()) {
                            showToast("המשתמש לא נמצא")
                            return@addOnSuccessListener
                        }

                        // Get recipient's wallet reference
                        db.collection("users")
                            .document(recipientUid)
                            .collection("wallet")
                            .whereEqualTo("cryptoName", selectedCrypto)
                            .get()
                            .addOnSuccessListener { recipientWallet ->
                                // Start transaction
                                db.runTransaction { transaction ->
                                    // Update sender's wallet
                                    val newSenderAmount = currentAmount - cryptoAmountToTransfer
                                    val senderWorth = senderAsset.getDouble("worth") ?: 0.0
                                    val transferWorth = (senderWorth / currentAmount) * cryptoAmountToTransfer
                                    
                                    if (newSenderAmount <= 0) {
                                        transaction.delete(senderAsset.reference)
                                    } else {
                                        transaction.update(senderAsset.reference, mapOf(
                                            "amount" to newSenderAmount,
                                            "worth" to (senderWorth - transferWorth),
                                            "timestamp" to com.google.firebase.Timestamp.now()
                                        ))
                                    }

                                    if (!recipientWallet.isEmpty) {
                                        // Update existing recipient's crypto
                                        val recipientAsset = recipientWallet.documents[0]
                                        val recipientAmount = recipientAsset.getDouble("amount") ?: 0.0
                                        val recipientWorth = recipientAsset.getDouble("worth") ?: 0.0
                                        
                                        transaction.update(recipientAsset.reference, mapOf(
                                            "amount" to (recipientAmount + cryptoAmountToTransfer),
                                            "worth" to (recipientWorth + transferWorth),
                                            "timestamp" to com.google.firebase.Timestamp.now()
                                        ))
                                    } else {
                                        // Create new crypto asset for recipient
                                        val newRecipientAssetRef = db.collection("users")
                                            .document(recipientUid)
                                            .collection("wallet")
                                            .document()

                                        val assetData = hashMapOf(
                                            "cryptoName" to selectedCrypto,
                                            "amount" to cryptoAmountToTransfer,
                                            "worth" to transferWorth,
                                            "buyPrice" to currentPrice,
                                            "timestamp" to com.google.firebase.Timestamp.now()
                                        )
                                        
                                        transaction.set(newRecipientAssetRef, assetData)
                                    }
                                }.addOnSuccessListener {
                                    showToast("ההעברה בוצעה בהצלחה")
                                    clearInputs()
                                }.addOnFailureListener { e ->
                                    showToast("שגיאה בהעברה: ${e.message}")
                                }
                            }
                    }
            }
    }

    private fun clearInputs() {
        recipientUidInput.text.clear()
        amountInput.text.clear()
        cryptoSelector.text.clear()
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserCryptos() {
        val currentUser = auth.currentUser ?: return

        // Get user's wallet contents
        db.collection("users")
            .document(currentUser.uid)
            .collection("wallet")
            .get()
            .addOnSuccessListener { documents ->
                val userCryptos = documents.mapNotNull { doc ->
                    val cryptoName = doc.getString("cryptoName") ?: return@mapNotNull null
                    val amount = doc.getDouble("amount") ?: 0.0
                    
                    CryptoListItem(
                        name = cryptoName,
                        symbol = getCryptoSymbol(cryptoName),
                        price = doc.getDouble("buyPrice") ?: 0.0,
                        id = getCryptoId(cryptoName)
                    ).apply {
                        // Add amount information to display
                        toString = { "$name (${String.format("%.8f", amount)})" }
                    }
                }

                // Update the AutoCompleteTextView adapter with only user's cryptos
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    userCryptos
                )
                cryptoSelector.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                showToast("שגיאה בטעינת הארנק: ${e.message}")
            }
    }

    private fun getCryptoSymbol(name: String): String {
        return when(name) {
            "Bitcoin" -> "BTC"
            "Ethereum" -> "ETH"
            "Binance Coin" -> "BNB"
            "Cardano" -> "ADA"
            "Solana" -> "SOL"
            "Ripple" -> "XRP"
            "Dogecoin" -> "DOGE"
            "Polkadot" -> "DOT"
            "Polygon" -> "MATIC"
            "Shiba Inu" -> "SHIB"
            else -> name.take(3).toUpperCase()
        }
    }

    private fun getCryptoId(name: String): Int {
        return when(name) {
            "Bitcoin" -> 1
            "Ethereum" -> 1027
            "Binance Coin" -> 1839
            "Cardano" -> 2010
            "Solana" -> 5426
            "Ripple" -> 52
            "Dogecoin" -> 74
            "Polkadot" -> 6636
            "Polygon" -> 3890
            "Shiba Inu" -> 5994
            else -> 1
        }
    }

    data class CryptoListItem(
        val name: String,
        val symbol: String,
        val price: Double,
        val id: Int,
        var toString: () -> String = { "$name ($symbol)" }
    ) {
        override fun toString(): String = toString.invoke()
    }
}

