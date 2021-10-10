package com.bairei.mobileelectricpowermeter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bairei.mobileelectricpowermeter.MeterListAdapter.MeterViewHolder
import com.bairei.mobileelectricpowermeter.data.Meter
import java.time.Duration

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

    class MeterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener {

        private val meterItemView: TextView = itemView.findViewById(R.id.meterTextView)
        val deleteMeterButton: Button = itemView.findViewById(R.id.deleteMeterButton)
        var meter: Meter? = null

        fun bind(meter: Meter?) {
            this.meter = meter
            meterItemView.text = meter?.asPrettyString()
            meterItemView.setOnLongClickListener(this)
        }

        override fun onLongClick(view: View): Boolean {
            val clipboardManager: ClipboardManager =
                view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val meterClip = ClipData.newPlainText("Raw Meter", meter?.asRawString())
            clipboardManager.setPrimaryClip(meterClip)

            Toast.makeText(
                view.context,
                "Raw representation copied to clipboard",
                Toast.LENGTH_SHORT
            ).show()

            return true
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
