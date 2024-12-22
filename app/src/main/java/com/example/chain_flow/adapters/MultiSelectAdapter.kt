package com.example.chain_flow.adapters

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class MultiSelectAdapter(
    private val context: Context,
    private val spinner: Spinner,
    private val items: Array<String>
) {
    private var selectedItem: String? = null

    init {
        // Create adapter with all cryptocurrency options
        val adapter = ArrayAdapter(
            context, 
            android.R.layout.simple_spinner_item,
            listOf("Select cryptocurrency") + items.toList()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Handle item selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Skip the hint item
                    selectedItem = items[position - 1]
                } else {
                    selectedItem = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedItem = null
            }
        }
    }

    fun getSelectedItem(): String? = selectedItem
}
