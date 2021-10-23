package com.bairei.mobileelectricpowermeter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
    private val rawTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

    private val bookmarkImportFilePicker =
        registerForActivityResult(StartActivityForResult(), this::handleFilePickerResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_meter_entry)
        measurementValueEditText = findViewById(R.id.measurementValueEditText)
        measurementDateEditText = findViewById(R.id.measurementDateEditText)
        measurementTimeEditText = findViewById(R.id.measurementTimeEditText)

        measurementDateEditText.setText(dateOfMeasurement.toString())
        measurementTimeEditText.setText(timeFormatter.format(timeOfMeasurement))

        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateOfMeasurement = LocalDate.of(year, monthOfYear, dayOfMonth)
                updateDateEditText()
            }

        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            timeOfMeasurement = LocalTime.of(hour, minute)
            updateTimeEditText()
        }

        measurementDateEditText.setOnClickListener {
            DatePickerDialog(
                this,
                onDateSetListener,
                dateOfMeasurement.year,
                dateOfMeasurement.monthValue,
                dateOfMeasurement.dayOfMonth
            ).show()
        }

        measurementTimeEditText.setOnClickListener {
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

        val uploadMultipleEntriesButton = findViewById<Button>(R.id.uploadMultipleEntriesButton)
        uploadMultipleEntriesButton.setOnClickListener {
            val fileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            bookmarkImportFilePicker.launch(fileIntent)
        }
    }

    private fun updateTimeEditText() {
        measurementTimeEditText.setText(timeOfMeasurement.toString())
    }

    private fun updateDateEditText() {
        measurementDateEditText.setText(dateOfMeasurement.toString())
    }

    private fun isMeasurementNotComplete(): Boolean {
        return TextUtils.isEmpty(measurementValueEditText.text) ||
            TextUtils.isEmpty(measurementDateEditText.text) ||
            TextUtils.isEmpty(measurementTimeEditText.text)
    }

    private fun handleFilePickerResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                applicationContext?.contentResolver?.openInputStream(uri).use { inputStream ->
                    val builder = StringBuilder()
                    inputStream?.bufferedReader()?.use {
                        builder.append(it.readText())
                        parseEntities(builder.toString())
                    }
                }
            }
        } else {
            Log.w(TAG, "handleFilePickerResult: Cancel parsing file, received ${result.resultCode}")
        }
    }

    /**
     * Parse lines based on pattern "HHmm ddMM XXX,X" into a collection of Meter entities,
     * which then is returned to MainActivity to be saved in the repository,
     * finishing the current activity
     * @param contentAsString - The entire file content in a String representation
     */
    private fun parseEntities(contentAsString: String?) {
        val multiplyMetersIntent = Intent()
        if (contentAsString != null) {
            val textAsLines = contentAsString.lines()
            Log.i(TAG, "parseEntities: text as lines: $textAsLines")
            val entries = textAsLines.map { line ->
                val splitLine = line.split(" ")
                val rawTime = splitLine[0]
                val rawDate = splitLine[1]
                val rawValue = splitLine[2].replace(",", ".")
                val localDate = LocalDate.of(
                    LocalDate.now().year,
                    rawDate.substring(2).toInt(),
                    rawDate.substring(0, 2).toInt()
                )
                val localTime = LocalTime.parse(rawTime, rawTimeFormatter)
                val value = BigDecimal(rawValue, MathContext.DECIMAL64).multiply(BigDecimal.TEN)
                    .intValueExact()

                Meter(
                    readingDate = LocalDateTime.of(localDate, localTime),
                    meterReading = value
                )
            }.toCollection(ArrayList())

            multiplyMetersIntent.putParcelableArrayListExtra(MULTIPLE_REPLIES, entries)
            setResult(RESULT_OK, multiplyMetersIntent)
        } else {
            Log.i(TAG, "parseEntities: no text was provided")
            setResult(Activity.RESULT_CANCELED, multiplyMetersIntent)
        }
        finish()
    }

    companion object {
        const val TAG = "NewMeterEntryActivity"
        const val EXTRA_REPLY = "com.bairei.mobileelectricpowermeter.REPLY"
        const val MULTIPLE_REPLIES = "com.bairei.mobileelectricpowermeter.MULTIPLE_REPLIES"
    }
}
