package com.example.calculator

import android.app.ActivityOptions
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import com.example.calculator.api.Request
import kotlinx.coroutines.android.awaitFrame
import java.net.HttpURLConnection

class ExchangeActivity : AppCompatActivity() {
    private var currencies = mutableMapOf<String?, Double?>(
        "EUR" to 1.0,
        "USD" to 1.07,
    )

    private lateinit var spinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)

        adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner,
            currencies.keys.toList(),
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner = findViewById(R.id.spinner1)
        spinner.adapter = adapter

        spinner = findViewById(R.id.spinner2)
        spinner.adapter = adapter

        fetchCurrencyData().start()
    }

    private fun fetchCurrencyData(): Thread {
        return Thread {
            val url = URL("http://data.fixer.io/api/latest?access_key=b8adc236d7fba89e78699c105a7dfdb2")
            val connection = url.openConnection() as HttpURLConnection
            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, Request::class.java)
                if (request.success) {
                    currencies = request.rates.toMutableMap()
                    this@ExchangeActivity.runOnUiThread(Runnable{
                        updateSpinnerData()
                    })
                } else {
                    this@ExchangeActivity.runOnUiThread(Runnable{
                        Toast.makeText(
                            applicationContext,
                            "Error requesting currencies: ${request.error.info}",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                }
                inputStreamReader.close()
                inputSystem.close()
            } else {
                this@ExchangeActivity.runOnUiThread(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Error connection: ${connection.responseCode}",
                        Toast.LENGTH_LONG
                    ).show()
                })
            }
        }
    }

    private fun updateSpinnerData() {
        adapter.clear()
        adapter.addAll(currencies.keys.toList())
        adapter.notifyDataSetChanged()

        spinner = findViewById(R.id.spinner1)
        spinner.setSelection("EUR".let { adapter.getPosition(it) })

        spinner = findViewById(R.id.spinner2)
        spinner.setSelection("USD".let { adapter.getPosition(it) })
    }

    private fun calculateExchange() {
        val input = findViewById<android.widget.TextView>(R.id.spinner_text1)
        val output = findViewById<android.widget.TextView>(R.id.spinner_text2)
        val inputText = input.text.toString()

        val inputCurrency = findViewById<Spinner>(R.id.spinner1).selectedItem.toString()
        val outputCurrency = findViewById<Spinner>(R.id.spinner2).selectedItem.toString()
        val inputRate = currencies[inputCurrency]
        val outputRate = currencies[outputCurrency]
        var result = inputText.toDouble() * (outputRate ?: 1.0) / (inputRate ?: 1.0)
        result = String.format("%.4f", result).replace(",", ".").toDouble()
        output.text = if(result % 1 == 0.0) result.toInt().toString() else result.toString()
        val outputText = output.text.toString()
        if (outputText.toDouble().toInt() == Int.MAX_VALUE) {
            output.text = "Overflow"
        }
    }

    fun buttonClicked(view: View) {
        val input = findViewById<android.widget.TextView>(R.id.spinner_text1)
        val output = findViewById<android.widget.TextView>(R.id.spinner_text2)
        val buttonText = findViewById<android.widget.Button>(view.id).text.toString()
        var inputText = input.text.toString()

        if (buttonText == "AC") {
            inputText = "0"
            input.text = inputText
        } else if (buttonText == "backspace") {
            if (inputText.length > 1) {
                inputText = inputText.substring(0, inputText.length - 1)
                input.text = inputText
            } else {
                inputText = "0"
                input.text = inputText
            }
        } else if (buttonText == ".") {
            if (!inputText.contains(".")) {
                inputText += "."
                input.text = inputText
            }
        } else {
            if (output.text.toString() == "Overflow") {
                return
            }
            if (inputText == "0") {
                inputText = buttonText
            } else {
                inputText += buttonText
            }
            input.text = inputText
        }

        calculateExchange()
    }

    fun changeActivity(view: View) {
        val intent = android.content.Intent(this, MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun swapCurrencies(view: View) {
        spinner = findViewById(R.id.spinner1)
        val temp1 = spinner.selectedItem.toString()

        spinner = findViewById(R.id.spinner2)
        val temp2 = spinner.selectedItem.toString()
        spinner.setSelection(temp1.let { adapter.getPosition(it) })

        spinner = findViewById(R.id.spinner1)
        spinner.setSelection(temp2.let { adapter.getPosition(it) })

        calculateExchange()
    }
}