package com.example.chain_flow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chain_flow.R

class TradeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (create a layout file named fragment_trade.xml)
        return inflater.inflate(R.layout.fragment_trade, container, false)
    }
}
