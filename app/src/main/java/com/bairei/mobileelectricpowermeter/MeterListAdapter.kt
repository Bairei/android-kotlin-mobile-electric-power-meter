package com.bairei.mobileelectricpowermeter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bairei.mobileelectricpowermeter.MeterListAdapter.MeterViewHolder
import com.bairei.mobileelectricpowermeter.data.Meter

class MeterListAdapter : ListAdapter<Meter, MeterViewHolder>(MetersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterViewHolder {
        return MeterViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MeterViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class MeterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener {

        private val meterDateTextView: TextView = itemView.findViewById(R.id.meterDateTextView)
        private val meterValueTextView: TextView = itemView.findViewById(R.id.meterValueTextView)
        var meter: Meter? = null

        fun bind(meter: Meter?) {
            this.meter = meter
            meterDateTextView.text = meter?.prettyDate()
            meterDateTextView.setOnLongClickListener(this)
            meterValueTextView.text = meter?.prettyReading()
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
            return oldItem.meterReading == newItem.meterReading &&
                oldItem.readingDate == newItem.readingDate
        }
    }
}
