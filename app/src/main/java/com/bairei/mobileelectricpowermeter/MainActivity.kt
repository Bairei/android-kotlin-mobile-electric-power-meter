package com.bairei.mobileelectricpowermeter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bairei.mobileelectricpowermeter.NewMeterEntryActivity.Companion.EXTRA_REPLY
import com.bairei.mobileelectricpowermeter.NewMeterEntryActivity.Companion.MULTIPLE_REPLIES
import com.bairei.mobileelectricpowermeter.data.Meter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private val createNewEntry =
        registerForActivityResult(StartActivityForResult(), this::handleNewEntryResult)
    private val meterViewModel: MeterViewModel by viewModels {
        MeterViewModelFactory((application as ElectricPowerMeterApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentCollectionAdapter = FragmentCollectionAdapter(supportFragmentManager, lifecycle)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager.adapter = fragmentCollectionAdapter
        // [bkwapisz] disable swiping pages to not delete meter entries by mistake
        viewPager.isUserInputEnabled = false
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.meter_list)
                1 -> tab.text = getString(R.string.meter_chart)
                else -> tab.text = getString(R.string.unknown_tab, position)
            }
        }.attach()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMeterEntryActivity::class.java)
            createNewEntry.launch(intent)
        }
    }

    private fun handleNewEntryResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            if (data.getParcelableExtra<Meter>(EXTRA_REPLY) != null) {
                data.getParcelableExtra<Meter>(EXTRA_REPLY)!!.let {
                    meterViewModel.insert(it)
                }
            } else if (data.getParcelableArrayListExtra<Meter>(MULTIPLE_REPLIES) != null) {
                val entries =
                    data.getParcelableArrayListExtra<Meter>(MULTIPLE_REPLIES)!!
                meterViewModel.insert(*entries.toTypedArray())
            }
        } else {
            Toast.makeText(applicationContext, R.string.invalid_not_saved, Toast.LENGTH_LONG).show()
        }
    }
}
