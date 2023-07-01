package com.location.reminder.sound.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.location.reminder.sound.databinding.ItemAddressBinding
import com.location.reminder.sound.model.AddressListModel

class AddressListAdapter(
    private val list: ArrayList<AddressListModel>,
    private val listener: ((Int) -> Unit)? = null
) :
    RecyclerView.Adapter<AddressListAdapter.AddressViewHolder>() {

    lateinit var binding: ItemAddressBinding

    inner class AddressViewHolder : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.txtAddress.text = list[position].placeName
            binding.txtSubAddress.text = list[position].placeDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder()
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(position)
        binding.root.setOnClickListener {
            listener?.invoke(position)
        }
    }

    override fun getItemCount(): Int = list.size

}


