package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.chain_flow.R
import com.google.android.material.button.MaterialButton

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation(view)
        setupBackButton(view)
    }

    protected fun setupBottomNavigation(view: View) {
        view.findViewById<MaterialButton>(R.id.home_button)?.setOnClickListener {
            navigateToFragment(HomeFragment())
        }

        view.findViewById<MaterialButton>(R.id.portfolio_button)?.setOnClickListener {
            navigateToFragment(PortfolioFragment())
        }
    }

    protected fun setupBackButton(view: View) {
        view.findViewById<ImageButton>(R.id.back_button)?.setOnClickListener {
            // Pop the back stack to return to the previous fragment
            requireActivity().onBackPressed()
        }
    }

    protected fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
} 