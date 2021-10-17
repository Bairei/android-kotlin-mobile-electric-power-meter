package com.bairei.mobileelectricpowermeter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bairei.mobileelectricpowermeter.data.Meter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val newMeterActivityRequestCode = 1

    private lateinit var adapter: MeterListAdapter
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var recyclerView: RecyclerView

    private val meterViewModel: MeterViewModel by viewModels {
        MeterViewModelFactory((application as ElectricPowerMeterApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        constraintLayout = findViewById(R.id.constraintLayout)
        recyclerView = findViewById(R.id.recycler_view)
        adapter = MeterListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        meterViewModel.allMeters.observe(this, { meters ->
            // Update the cached copy of the words in the adapter.
            meters?.let { adapter.submitList(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMeterEntryActivity::class.java)
            startActivityForResult(intent, newMeterActivityRequestCode)
        }

        val itemTouchHelper = swipeOnDelete()
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newMeterActivityRequestCode && resultCode == RESULT_OK) {
            if (data?.getParcelableExtra<Meter>(NewMeterEntryActivity.EXTRA_REPLY) != null) {
                data.getParcelableExtra<Meter>(NewMeterEntryActivity.EXTRA_REPLY)?.let {
                    meterViewModel.insert(it)
                }
            } else if (data?.getParcelableArrayListExtra<Meter>(NewMeterEntryActivity.MULTIPLE_REPLIES) != null) {
                val entries =
                    data.getParcelableArrayListExtra<Meter>(NewMeterEntryActivity.MULTIPLE_REPLIES)
                entries?.forEach { meterViewModel.insert(it) }
            }
        } else {
            Toast.makeText(applicationContext, R.string.invalid_not_saved, Toast.LENGTH_LONG).show()
        }
    }

    private fun swipeOnDelete(): ItemTouchHelper {
        val swipeItemCallback = object : DeleteSwipeItemCallback(this) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = adapter.currentList[position]

                meterViewModel.delete(itemToDelete)
                Snackbar.make(constraintLayout, R.string.meter_deleted, Snackbar.LENGTH_LONG)
                    .setAction(
                        R.string.undo
                    ) {
                        meterViewModel.insert(itemToDelete)
                        recyclerView.layoutManager?.scrollToPosition(position)
                    }
                    .show()
            }
        }
        return ItemTouchHelper(swipeItemCallback)
    }


}