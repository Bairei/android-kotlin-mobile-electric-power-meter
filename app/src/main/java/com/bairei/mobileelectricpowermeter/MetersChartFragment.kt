package com.bairei.mobileelectricpowermeter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bairei.mobileelectricpowermeter.data.Meter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.DateFormat
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

// TODO [bkwapisz] stub
class MetersChartFragment : Fragment() {

    private val meterViewModel: MeterViewModel by viewModels {
        MeterViewModelFactory(
            (requireActivity().application as ElectricPowerMeterApplication).repository
        )
    }

    private lateinit var adapter: MeterListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meters_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val graphView: GraphView = view.findViewById(R.id.graphView)
        meterViewModel.allMeters.observe(viewLifecycleOwner) { meters ->
            meters?.let { meterList ->
                val lineDataPoints = meterList.map {
                    val date = Date.from(it.readingDate.atZone(ZoneId.systemDefault()).toInstant())
                    DataPoint(date, it.meterReading.div(10.0))
                }
                val lineGraphSeries: LineGraphSeries<DataPoint> = LineGraphSeries(lineDataPoints.toTypedArray())
                graphView.addSeries(lineGraphSeries)
                // TODO [bkwapisz] add better bar graph to represent energy used?
//                val barDataPoints = meterList.mapIndexed {index, meter ->
//                    val date = Date.from(meter.readingDate.atZone(ZoneId.systemDefault()).toInstant())
//                    DataPoint(date, if (index == 0) 0.0 else (meterList[index - 1].meterReading - meter.meterReading).div(10.0))
//                }.filter { it.y > 0 }
//                val barGraphSeries: BarGraphSeries<DataPoint> = BarGraphSeries(barDataPoints.toTypedArray())
//                barGraphSeries.color = Color.rgb(0, 255, 0)
//                graphView.addSeries(barGraphSeries)
//                graphView.secondScale.addSeries(barGraphSeries)
//                graphView.secondScale.setMinY(0.0)
//                graphView.secondScale.setMaxY(barDataPoints.maxOf { it.y })
                graphView.gridLabelRenderer.apply {
                    numHorizontalLabels = lineDataPoints.size / 21
                    setHorizontalLabelsAngle(90)
                    labelHorizontalHeight = 240
                    labelFormatter = DateAsXAxisLabelFormatter(context, DateFormat.getDateInstance())
                }
            }
        }

    }

    companion object {
        fun newInstance() = MetersChartFragment()
    }
}
