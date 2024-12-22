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
        val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0

        // Validate amount
        if (amount < 10.0 || amount > 30000.0) {
            Toast.makeText(requireContext(), "Amount must be between $10 and $30,000", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Check user's balance first
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val currentBalance = document.getDouble("balance") ?: 10000.0

                if (amount > currentBalance) {
                    Toast.makeText(requireContext(), "Insufficient balance", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Show loading toast
                Toast.makeText(requireContext(), "Processing purchase...", Toast.LENGTH_SHORT).show()

                // Calculate crypto amount
                val cryptoAmount = amount / rawPrice
                val cryptoName = cryptoName.text.toString()

                // Reference to user's wallet collection
                val walletRef = db.collection("users")
                    .document(user.uid)
                    .collection("Wallet")

                // Check if user already owns this crypto
                walletRef.whereEqualTo("cryptoName", cryptoName)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // User already owns this crypto - update existing document
                            val existingDoc = querySnapshot.documents[0]
                            val existingAmount = existingDoc.getDouble("amount") ?: 0.0
                            val existingWorth = existingDoc.getDouble("worth") ?: 0.0
                            
                            // Calculate new average buy price
                            val totalWorth = existingWorth + amount
                            val totalAmount = existingAmount + cryptoAmount
                            val newAverageBuyPrice = totalWorth / totalAmount

                            val updates = hashMapOf<String, Any>(
                                "amount" to totalAmount,
                                "worth" to totalWorth,
                                "buyPrice" to newAverageBuyPrice,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            existingDoc.reference.update(updates)
                                .addOnSuccessListener {
                                    updateBalanceAndFinish(user.uid, currentBalance, amount)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to update: ${e.message}", 
                                        Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // User doesn't own this crypto - create new document
                            val newWalletData = hashMapOf(
                                "amount" to cryptoAmount,
                                "worth" to amount,
                                "buyPrice" to rawPrice,
                                "timestamp" to com.google.firebase.Timestamp.now(),
                                "cryptoName" to cryptoName
                            )

                            walletRef.add(newWalletData)
                                .addOnSuccessListener {
                                    updateBalanceAndFinish(user.uid, currentBalance, amount)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Transaction failed: ${e.message}", 
                                        Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to check existing assets: ${e.message}", 
                            Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to check balance: ${e.message}", 
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun handleSellAction() {
        val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Get user's wallet to check if they have enough crypto to sell
        db.collection("users").document(user.uid)
            .collection("Wallet")
            .whereEqualTo("cryptoName", cryptoName.text.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "You don't own this cryptocurrency", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val asset = documents.documents[0]
                val currentAmount = asset.getDouble("amount") ?: 0.0
                val cryptoToSell = amount / rawPrice

                if (cryptoToSell > currentAmount) {
                    Toast.makeText(context, "Insufficient crypto balance", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Calculate new amount and worth
                val newAmount = currentAmount - cryptoToSell
                val currentWorth = asset.getDouble("worth") ?: 0.0
                val soldWorth = (currentWorth / currentAmount) * cryptoToSell
                val newWorth = currentWorth - soldWorth

                if (newAmount <= 0) {
                    // Delete the asset if selling all
                    asset.reference.delete()
                } else {
                    // Update the asset with new amounts
                    asset.reference.update(
                        mapOf(
                            "amount" to newAmount,
                            "worth" to newWorth
                        )
                    )
                }

                // Update user's balance
                db.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val currentBalance = userDoc.getDouble("balance") ?: 0.0
                        val newBalance = currentBalance + amount

                        userDoc.reference.update("balance", newBalance)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Sale successful!", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBalanceAndFinish(userId: String, currentBalance: Double, amount: Double) {
        // Update user's balance
        val newBalance = currentBalance - amount
        db.collection("users").document(userId)
            .update("balance", newBalance)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Purchase successful!", Toast.LENGTH_LONG).show()
                // Navigate back
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update balance: ${e.message}", 
                    Toast.LENGTH_LONG).show()
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