package com.example.chain_flow.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chain_flow.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class BuyFragment : BaseFragment() {
    private lateinit var cryptoIcon: ImageView
    private lateinit var cryptoName: TextView
    private lateinit var cryptoPrice: TextView
    private lateinit var cryptoSymbol: TextView
    private lateinit var amountInput: EditText
    private lateinit var amountToReceive: TextView
    private lateinit var buyButton: Button
    private lateinit var sellButton: Button
    private var rawPrice: Double = 0.0
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)
        initializeViews(view)
        setupListeners()
        return view
    }

    private fun initializeViews(view: View) {
        cryptoIcon = view.findViewById(R.id.crypto_icon)
        cryptoName = view.findViewById(R.id.crypto_name)
        cryptoPrice = view.findViewById(R.id.crypto_price)
        cryptoSymbol = view.findViewById(R.id.crypto_symbol)
        amountInput = view.findViewById(R.id.amount_input)
        amountToReceive = view.findViewById(R.id.amount_to_receive)
        buyButton = view.findViewById(R.id.buy_button)
        sellButton = view.findViewById(R.id.sell_button)

        buyButton.setOnClickListener {
            handleBuyAction()
        }

        sellButton.setOnClickListener {
            handleSellAction()
        }

        // Set initial texts
        cryptoSymbol.text = "Bitcoin (BTC)"
        amountToReceive.text = "You will receive: 0.00000000 Bitcoin (BTC)"
    }

    private fun setupListeners() {
        // Back button
        view?.findViewById<ImageButton>(R.id.back_button)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Amount input listener
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateCryptoAmount(s?.toString())
            }
        })
    }

    private fun calculateCryptoAmount(amountStr: String?) {
        val usdAmount = amountStr?.toDoubleOrNull() ?: 0.0
        if (rawPrice > 0) {
            val cryptoAmount = usdAmount / rawPrice
            amountToReceive.text = String.format("You will receive: %.8f Bitcoin (BTC)", cryptoAmount)
        }
    }

    private fun handleBuyAction() {
        val amount = amountInput.text.toString().toDoubleOrNull() ?: run {
            showToast("נא להזין סכום תקין")
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: run {
            showToast("יש להתחבר תחילה")
            return
        }

        // Check if user exists and has balance
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create new user with initial balance if doesn't exist
                    val userData = hashMapOf(
                        "email" to user.email,
                        "balance" to 1000000000.0,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            processPurchase(user.uid, amount, 1000000000.0)
                        }
                        .addOnFailureListener { e ->
                            showToast("שגיאה ביצירת משתמש: ${e.message}")
                        }
                } else {
                    val currentBalance = document.getDouble("balance") ?: 1000000000.0
                    if (currentBalance < amount) {
                        showToast("אין מספיק יתרה בחשבון")
                        return@addOnSuccessListener
                    }
                    processPurchase(user.uid, amount, currentBalance)
                }
            }
            .addOnFailureListener { e ->
                showToast("שגיאה בבדיקת היתרה: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSellAction() {
        val amount = amountInput.text.toString().toDoubleOrNull() ?: run {
            showToast("Please enter a valid amount")
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: run {
            showToast("Please login first")
            return
        }

        val cryptoToSell = amount / rawPrice
        val cryptoName = cryptoName.text.toString()

        // First get the wallet data
        db.collection("users")
            .document(user.uid)
            .collection("wallet")
            .whereEqualTo("cryptoName", cryptoName)
            .get()
            .addOnSuccessListener { walletSnapshot ->
                if (walletSnapshot.isEmpty) {
                    showToast("You don't own this cryptocurrency")
                    return@addOnSuccessListener
                }

                // Start transaction after we have the wallet data
                db.runTransaction { transaction ->
                    val asset = walletSnapshot.documents[0]
                    val currentAmount = asset.getDouble("amount") ?: 0.0
                    
                    if (currentAmount < cryptoToSell) {
                        throw Exception("Insufficient crypto balance")
                    }

                    val newAmount = currentAmount - cryptoToSell
                    val currentWorth = asset.getDouble("worth") ?: 0.0
                    val soldWorth = (currentWorth / currentAmount) * cryptoToSell
                    val newWorth = currentWorth - soldWorth

                    // Get user's current balance
                    val userRef = db.collection("users").document(user.uid)
                    val userDoc = transaction.get(userRef)
                    val currentBalance = userDoc.getDouble("balance") ?: 0.0

                    if (newAmount <= 0) {
                        // Delete the asset if selling all
                        transaction.delete(asset.reference)
                    } else {
                        // Update the asset with new amounts
                        transaction.update(asset.reference, mapOf(
                            "amount" to newAmount,
                            "worth" to newWorth,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        ))
                    }

                    // Update user's balance
                    transaction.update(userRef, "balance", currentBalance + amount)
                }.addOnSuccessListener {
                    showToast("Sale successful!")
                    parentFragmentManager.popBackStack()
                }.addOnFailureListener { e ->
                    showToast("Sale failed: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to check wallet: ${e.message}")
            }
    }

    private fun processPurchase(userId: String, amount: Double, currentBalance: Double) {
        val cryptoAmount = amount / rawPrice
        val cryptoName = cryptoName.text.toString()

        // Get user's wallet reference
        db.collection("users")
            .document(userId)
            .collection("wallet")
            .whereEqualTo("cryptoName", cryptoName)
            .get()
            .addOnSuccessListener { walletSnapshot ->
                // Start transaction after we have the wallet data
                db.runTransaction { transaction ->
                    // Get fresh user data
                    val userRef = db.collection("users").document(userId)
                    val userDoc = transaction.get(userRef)
                    val updatedBalance = userDoc.getDouble("balance") ?: currentBalance

                    if (updatedBalance < amount) {
                        throw Exception("אין מספיק יתרה בחשבון")
                    }

                    if (!walletSnapshot.isEmpty) {
                        // Update existing crypto
                        val existingAsset = walletSnapshot.documents[0]
                        val currentAmount = existingAsset.getDouble("amount") ?: 0.0
                        
                        val newAmount = currentAmount + cryptoAmount
                        val newWorth = newAmount * rawPrice

                        transaction.update(existingAsset.reference, mapOf(
                            "amount" to newAmount,
                            "worth" to newWorth,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        ))
                    } else {
                        // Create new crypto asset
                        val newAssetRef = db.collection("users")
                            .document(userId)
                            .collection("wallet")
                            .document()

                        val assetData = hashMapOf(
                            "cryptoName" to cryptoName,
                            "amount" to cryptoAmount,
                            "worth" to amount,
                            "buyPrice" to rawPrice,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        
                        transaction.set(newAssetRef, assetData)
                    }

                    // Update user's balance
                    transaction.update(userRef, "balance", updatedBalance - amount)
                }.addOnSuccessListener {
                    showToast("הרכישה בוצעה בהצלחה!")
                    parentFragmentManager.popBackStack()
                }.addOnFailureListener { e ->
                    showToast("שגיאה ברכישה: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                showToast("שגיאה בבדיקת הארנק: ${e.message}")
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get data from arguments
        arguments?.let { args ->
            cryptoName.text = args.getString("cryptoName", "Bitcoin")
            rawPrice = args.getDouble("rawPrice", 0.0)
            cryptoPrice.text = String.format("$%.2f", rawPrice)

            // Load crypto icon
            val imageUrl = args.getString("imageUrl", "")
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.bitcoin)
                .error(R.drawable.bitcoin)
                .into(cryptoIcon)
        }
    }
}