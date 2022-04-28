package com.bairei.mobileelectricpowermeter

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.bairei.mobileelectricpowermeter.data.Meter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class NewMeterEntryActivity : AppCompatActivity() {

    private lateinit var measurementValueEditText: EditText
    private lateinit var measurementDateEditText: EditText
    private lateinit var measurementTimeEditText: EditText

    private var dateOfMeasurement: LocalDate = LocalDate.now()
    private var timeOfMeasurement: LocalTime = LocalTime.now()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val rawTimeFormatter = DateTimeFormatter.ofPattern("HHmm")
    private val timestampTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    private val bookmarkImportFilePicker =
        registerForActivityResult(StartActivityForResult(), this::handleFilePickerResult)
    private val exportEntriesFilePicker =
        registerForActivityResult(
            StartActivityForResult(),
            this::handleEntriesFileDirectoryPickerResult
        )

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
                // monthOfYear + 1, since LocalDate.of accepts values 1-12 for months,
                // meanwhile OnDateSetListener provides  0-11 (Calendar.MONTH) format
                dateOfMeasurement = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
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
                // monthOfYear -1, since DatePickerDialog required 0-11 (Calendar.MONTH) format
                dateOfMeasurement.monthValue - 1,
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

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
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
                type = "*/*"
            }
            bookmarkImportFilePicker.launch(fileIntent)
        }

        val exportEntriesButton = findViewById<Button>(R.id.exportEntriesButton)
        exportEntriesButton.setOnClickListener {
            val fileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, getFileTitle())
            }
            exportEntriesFilePicker.launch(fileIntent)
        }
    }

    private fun getFileTitle() = timestampTimeFormatter.format(LocalDateTime.now())

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
                        parseEntities(builder.toString(), isJsonFile(uri))
                    }
                }
            }
        } else {
            Log.w(TAG, "handleFilePickerResult: Cancel parsing file, received ${result.resultCode}")
        }
    }

    /**
     * Check if provided Uri leads to a file with a .json extension.
     *
     * @param uri a Uri object, pointing to a file in the filesystem, should not be null
     * @return true, if Uri points to a file, which has a .json extension, or false if otherwise
     */
    private fun isJsonFile(uri: Uri): Boolean = uri.path?.lowercase()?.endsWith(".json") ?: false

    private fun handleEntriesFileDirectoryPickerResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val exportIntent = Intent()
                exportIntent.putExtra(EXPORT_ENTRIES, uri.toString())

                (application as ElectricPowerMeterApplication).applicationScope.launch {
                    exportToFile(uri)
                }

                setResult(RESULT_OK, exportIntent)
            }
        } else {
            Log.w(
                TAG,
                "handleEntriesFileDirectoryPickerResult: " +
                    "Cancel parsing file, received ${result.resultCode}"
            )
        }
        finish()
    }

    /**
     * Parse lines based on either pattern "HHmm ddMM XXX,X" into a collection of Meter entities,
     * or from a Json array (depending on if the provided file has a .json file extension or not),
     * which then is returned to MainActivity to be saved in the repository,
     * finishing the current activity
     * @param contentAsString - The entire file content in a String representation
     */
    private fun parseEntities(contentAsString: String?, jsonFile: Boolean) {
        val multiplyMetersIntent = Intent()
        if (contentAsString != null) {
            val entries =
                if (jsonFile) parseJsonEntities(contentAsString)
                else parseCsvEntities(contentAsString)

            multiplyMetersIntent.putParcelableArrayListExtra(MULTIPLE_REPLIES, entries)
            setResult(RESULT_OK, multiplyMetersIntent)
        } else {
            Log.i(TAG, "parseEntities: no text was provided")
            setResult(Activity.RESULT_CANCELED, multiplyMetersIntent)
        }
        finish()
    }

    /**
     * Parse raw JSON collection to a list of Meter entities.
     *
     * @param contentAsString a JSON collection, as a raw String, should not be null
     * @return a collection of Meter entities
     */
    private fun parseJsonEntities(contentAsString: String): ArrayList<Meter> {
        val typeToken = object : TypeToken<ArrayList<Meter>>() {}.type
        return gson.fromJson(contentAsString, typeToken)
    }

    /**
     * Parse lines based on either pattern "HHmm ddMM XXX,X" into a collection of Meter entities,
     * which then is returned to MainActivity to be saved in the repository,
     * finishing the current activity
     * @param contentAsString - The entire file content in a String representation
     */
    private fun parseCsvEntities(contentAsString: String): ArrayList<Meter> {
        val textAsLines = contentAsString.lines()
        Log.i(TAG, "parseEntities: text as lines: $textAsLines")
        return textAsLines.map { line ->
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
    }

    /**
     * Export data from MeterDao (Room database) to given file.
     *
     * @param uri a Uri object, pointing to the file, where the exported data is going to be stored,
     * should not be empty
     *
     */
    private suspend fun exportToFile(uri: Uri) = coroutineScope {
        Log.i(TAG, "Exporting data...")
        val meterData =
            (application as ElectricPowerMeterApplication).database.meterDao().findAllSuspend()
        val outputJson = Gson().toJson(meterData)
        contentResolver.openFileDescriptor(uri, "w").use {
            FileOutputStream(it?.fileDescriptor).use {
                it.write(outputJson.toByteArray())
            }
        }
        Log.i(TAG, "exported successfully")
    }

    companion object {
        const val TAG = "NewMeterEntryActivity"
        const val EXTRA_REPLY = "com.bairei.mobileelectricpowermeter.REPLY"
        const val MULTIPLE_REPLIES = "com.bairei.mobileelectricpowermeter.MULTIPLE_REPLIES"
        const val EXPORT_ENTRIES = "com.bairei.mobileelectricpowermeter.EXPORT_ENTRIES"
    }
}
