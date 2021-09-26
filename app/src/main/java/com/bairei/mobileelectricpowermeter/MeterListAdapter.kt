package com.bairei.mobileelectricpowermeter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bairei.mobileelectricpowermeter.MeterListAdapter.MeterViewHolder
import com.bairei.mobileelectricpowermeter.data.Meter

class MeterListAdapter : ListAdapter<Meter, MeterViewHolder>(MetersComparator()) {

    lateinit var listener: ItemRemovedListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterViewHolder {
        return MeterViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MeterViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)

        holder.deleteMeterButton.setOnClickListener { view ->
            listener.onItemToRemoveClicked(current, position)
        }
    }

    class MeterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val meterItemView: TextView = itemView.findViewById(R.id.meterTextView)
        val deleteMeterButton: Button = itemView.findViewById(R.id.deleteMeterButton)

        fun bind(meter: Meter?) {
            meterItemView.text = meter?.asPrettyString()
        }


        companion object {
            fun create(parent: ViewGroup): MeterViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return MeterViewHolder(view)
            }
        }

    }

    class MetersComparator : DiffUtil.ItemCallback<Meter>() {
        override fun areItemsTheSame(oldItem: Meter, newItem: Meter): Boolean {
            return oldItem.businessId == newItem.businessId
        }

        override fun areContentsTheSame(oldItem: Meter, newItem: Meter): Boolean {
            return oldItem.meterReading == newItem.meterReading
                    && oldItem.readingDate == newItem.readingDate
        }

    }

    interface ItemRemovedListener {
        fun onItemToRemoveClicked(meter: Meter, position: Int)
    }

}
