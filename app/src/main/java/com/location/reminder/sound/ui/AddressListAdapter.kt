package com.location.reminder.sound.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.location.reminder.sound.R
import com.location.reminder.sound.model.AddressListModel
import kotlinx.android.synthetic.main.item_address.view.*

class AddressListAdapter(
    private val list: ArrayList<AddressListModel>,
    private val listener: AddressClickListener
) :
    RecyclerView.Adapter<AddressListAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            view.txtAddress.text = list[position].placeName
            view.txtSubAddress.text = list[position].placeDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(position)
        holder.view.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface AddressClickListener {
        fun onItemClick(position: Int)
    }
}


