package com.bairei.mobileelectricpowermeter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bairei.mobileelectricpowermeter.data.Meter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MeterListAdapter.ItemRemovedListener {

    private val newMeterActivityRequestCode = 1

    private val meterViewModel: MeterViewModel by viewModels {
        MeterViewModelFactory((application as ElectricPowerMeterApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = MeterListAdapter()
        adapter.listener = this
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newMeterActivityRequestCode && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Meter>(NewMeterEntryActivity.EXTRA_REPLY)?.let {
                meterViewModel.insert(it)
            }
        } else {
            Toast.makeText(applicationContext, R.string.invalid_not_saved, Toast.LENGTH_LONG).show()
        }
    }

    override fun onItemToRemoveClicked(meter: Meter, position: Int) {
        meterViewModel.delete(meter)
    }
}