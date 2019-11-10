package com.trainerapp.feature.add_event

import android.content.Context
import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AddEventLocationAutocompleteAdapter(
        context: Context,
        private val onclick: (Address) -> Unit
) : ArrayAdapter<Address>(context, android.R.layout.simple_list_item_1) {

    private val addresses = mutableListOf<Address>()

    override fun getCount(): Int {
        return addresses.size
    }

    override fun getItem(position: Int): Address {
        return addresses[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = convertView
                ?: LayoutInflater.from(context!!).inflate(android.R.layout.simple_list_item_1, parent, false)
        val address = getItem(position)

        val view = layout.findViewById<TextView>(android.R.id.text1)
        view.text = address.getAddressLine(0)
        view.setOnClickListener {
            onclick(address)
        }

        return view
    }

    fun setAddresses(addresses: List<Address>) {
        this.addresses.clear()
        this.addresses.addAll(addresses)
        notifyDataSetChanged()
    }
}
