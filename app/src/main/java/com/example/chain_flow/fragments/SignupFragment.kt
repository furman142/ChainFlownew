package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chain_flow.R
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        
        mAuth = FirebaseAuth.getInstance()
        
        val emailInput = view.findViewById<EditText>(R.id.signup_email)
        val passwordInput = view.findViewById<EditText>(R.id.signup_password)
        val confirmPasswordInput = view.findViewById<EditText>(R.id.signup_confirm_password)
        val signupButton = view.findViewById<Button>(R.id.signup_button)
        val loginText = view.findViewById<TextView>(R.id.login_text)
        
        signupButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            
            if (validateInputs(email, password, confirmPassword)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Switch to login fragment after successful signup
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()
                            Toast.makeText(context, "Signup successful! Please login.", 
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Signup failed: ${task.exception?.message}", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        
        loginText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
        
        return view
    }
    
    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
} 