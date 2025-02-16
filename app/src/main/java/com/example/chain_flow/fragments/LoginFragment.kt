package com.example.chain_flow.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chain_flow.models.Wallet
import com.example.chain_flow.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore


class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
   private val db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        mAuth = FirebaseAuth.getInstance()

        val emailInput = view.findViewById<EditText>(R.id.email_input)
        val passwordInput = view.findViewById<EditText>(R.id.password_input)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val signupText = view.findViewById<TextView>(R.id.signup_text)

        emailInput.setBackgroundResource(R.drawable.input_background)
        passwordInput.setBackgroundResource(R.drawable.input_background)
        loginButton.setBackgroundResource(R.drawable.button_background)

        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.lowercase() == "admin") {
                    // Admin shortcut - go directly to home
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
            }
        })

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                handleLogin(email, password)
            } else {
                Toast.makeText(context, "Please enter email and password",
                    Toast.LENGTH_SHORT).show()
            }
        }

        signupText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignupFragment())
                .commit()
        }

        return view
    }

    private fun handleLogin(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        // Check if user has balance data
                        db.collection("users").document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (!document.exists() || document.getDouble("balance") == null) {
                                    // Initialize user data if it doesn't exist
                                    val userData = hashMapOf(
                                        "email" to email,
                                        "balance" to 1000000000.0,  // 1B USD starting balance
                                        "createdAt" to com.google.firebase.Timestamp.now()
                                    )
                                    
                                    db.collection("users").document(user.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            navigateToPortfolio()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error initializing user data: ${e.message}", 
                                                Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    navigateToPortfolio()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error checking user data: ${e.message}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "Login failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToPortfolio() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PortfolioFragment())
            .commit()
    }
}