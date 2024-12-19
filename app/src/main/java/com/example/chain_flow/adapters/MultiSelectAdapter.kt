package com.example.chain_flow.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import java.util.ArrayList

class MultiSelectAdapter(
    private val context: Context,
    private val spinner: Spinner,
    private val items: Array<String>
) {
    private val selectedItems = ArrayList<String>()

    init {
        spinner.setOnClickListener {
            showMultiSelectDialog()
        }
    }

    private fun showMultiSelectDialog() {
        val checkedItems = BooleanArray(items.size) { selectedItems.contains(items[it]) }

        AlertDialog.Builder(context)
            .setTitle("Select Cryptocurrencies")
            .setMultiChoiceItems(items, checkedItems) { _, index, isChecked ->
                if (isChecked) {
                    selectedItems.add(items[index])
                } else {
                    selectedItems.remove(items[index])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                updateSpinnerText()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSpinnerText() {
        val text = if (selectedItems.isEmpty()) {
            "Select cryptocurrencies"
        } else {
            selectedItems.joinToString(", ")
        }
        
        // Create and set adapter with selected text
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, arrayOf(text))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    fun getSelectedItems(): List<String> = selectedItems
}
