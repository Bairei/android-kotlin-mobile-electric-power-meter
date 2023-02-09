package com.bairei.mobileelectricpowermeter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MeterListFragment : Fragment() {

    private val meterViewModel: MeterViewModel by viewModels {
        MeterViewModelFactory(
            (requireActivity().application as ElectricPowerMeterApplication).repository
        )
    }

    private lateinit var adapter: MeterListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("MeterListFragment", "On Create view")
        return inflater.inflate(R.layout.meter_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("MeterListFragment", "On View created")

        recyclerView = requireView().findViewById(R.id.recycler_view)
        adapter = MeterListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        meterViewModel.latestMeters.observe(viewLifecycleOwner) { meters ->
            // Update the cached copy of the words in the adapter.
            meters?.let { adapter.submitList(it) }
        }

        val itemTouchHelper = swipeOnDelete()
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun swipeOnDelete(): ItemTouchHelper {
        val swipeItemCallback = object : DeleteSwipeItemCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = adapter.currentList[position]

                meterViewModel.delete(itemToDelete)
                Snackbar.make(requireView(), R.string.meter_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        meterViewModel.insert(itemToDelete)
                        recyclerView.layoutManager
                            ?.scrollToPosition(if (position - 1 < 0) 0 else position - 1)
                    }.show()
            }
        }
        return ItemTouchHelper(swipeItemCallback)
    }

    companion object {
        fun newInstance() = MeterListFragment()
    }
}
