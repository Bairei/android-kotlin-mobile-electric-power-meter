package com.bairei.mobileelectricpowermeter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bairei.mobileelectricpowermeter.data.Meter
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NewMeterEntryActivity : AppCompatActivity() {

    private lateinit var measurementValueEditText: EditText
    private lateinit var measurementDateEditText: EditText
    private lateinit var measurementTimeEditText: EditText

    private var dateOfMeasurement: LocalDate = LocalDate.now()
    private var timeOfMeasurement: LocalTime = LocalTime.now()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_meter_entry)
        measurementValueEditText = findViewById(R.id.measurementValueEditText)
        measurementDateEditText = findViewById(R.id.measurementDateEditText)
        measurementTimeEditText = findViewById(R.id.measurementTimeEditText)

        measurementDateEditText.setText(dateOfMeasurement.toString())
        measurementTimeEditText.setText(timeFormatter.format(timeOfMeasurement))

        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                dateOfMeasurement = LocalDate.of(year, monthOfYear, dayOfMonth)
                updateDateEditText()
            }

        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            timeOfMeasurement = LocalTime.of(hour, minute)
            updateTimeEditText()
        }

        measurementDateEditText.setOnClickListener { view ->
            DatePickerDialog(
                this,
                onDateSetListener,
                dateOfMeasurement.year,
                dateOfMeasurement.monthValue,
                dateOfMeasurement.dayOfMonth
            ).show()
        }

        measurementTimeEditText.setOnClickListener { view ->
            TimePickerDialog(
                this,
                onTimeSetListener,
                timeOfMeasurement.hour,
                timeOfMeasurement.minute, true
            ).show()
        }

        val button = findViewById<Button>(R.id.saveButton)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (isMeasurementNotComplete()) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val measurementDate = LocalDateTime.of(dateOfMeasurement, timeOfMeasurement)
                val measurementValue =
                    BigDecimal(measurementValueEditText.text.toString(), MathContext.DECIMAL64)
                val measurementValueRounded =
                    measurementValue.multiply(BigDecimal.TEN).intValueExact()
                val meter =
                    Meter(readingDate = measurementDate, meterReading = measurementValueRounded)
                replyIntent.putExtra(EXTRA_REPLY, meter)
                setResult(RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    private fun updateTimeEditText() {
        measurementTimeEditText.setText(timeOfMeasurement.toString())
    }

    private fun updateDateEditText() {
        measurementDateEditText.setText(dateOfMeasurement.toString())
    }

    private fun isMeasurementNotComplete(): Boolean {
        return TextUtils.isEmpty(measurementValueEditText.text)
                || TextUtils.isEmpty(measurementDateEditText.text)
                || TextUtils.isEmpty(measurementTimeEditText.text)
    }

    companion object {
        const val EXTRA_REPLY = "com.bairei.mobileelectricpowermeter.REPLY"
    }
}