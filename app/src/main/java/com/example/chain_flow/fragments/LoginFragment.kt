package com.example.chain_flow.fragments

import HomeFragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser



class LoginFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    
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
        
        // Add text change listener for admin shortcut
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
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user: FirebaseUser? = mAuth.getCurrentUser()
                            val uid  = user?.uid
                            val userWallet = Wallet(userId = uid!!)





                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HomeFragment())
                                .commit()
                        } else {
                            Toast.makeText(context, "Login failed: ${task.exception?.message}", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
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
} 