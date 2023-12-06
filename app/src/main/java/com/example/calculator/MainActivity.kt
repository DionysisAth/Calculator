package com.example.calculator

import android.annotation.SuppressLint
import android.app.ActivityOptions
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private var resultShown = false
    private val buttons = mapOf(
        "0" to "digit",
        "1" to "digit",
        "2" to "digit",
        "3" to "digit",
        "4" to "digit",
        "5" to "digit",
        "6" to "digit",
        "7" to "digit",
        "8" to "digit",
        "9" to "digit",
        "." to "operator",
        "+" to "operator",
        "-" to "operator",
        "*" to "operator",
        "/" to "operator",
        "%" to "operator",
        "AC" to "special",
        "C" to "special",
        "backspace" to "special",
        "=" to "special",
    )

    fun buttonClicked(view: View) {
        val inputText = findViewById<android.widget.TextView>(R.id.input_text)
        val resultText = findViewById<android.widget.TextView>(R.id.result_text)
        val buttonText = findViewById<android.widget.Button>(view.id).text.toString()
        var evaluate = true

        if (resultShown) {
            changeMainText(inputText, resultText)
            clearText(true)
            resultShown = false
        }

        if (buttons[buttonText] != "special") {
            enterInput(buttonText)
        } else {
            if (buttonText == "AC") {
                clearText(true)
                evaluate = false
            } else if (buttonText == "C") {
                clearText(false)
                evaluate = false
            } else if (buttonText == "backspace") {
                val input = inputText.text.toString()
                if (input.length > 1) {
                    inputText.text = input.substring(0, input.length - 1)
                } else {
                    inputText.text = "0"
                }
            } else if (buttonText == "=") {
                if (resultText.visibility == View.VISIBLE) {
                    changeMainText(resultText, inputText)
                    resultShown = true
                } else {
                    evaluate = false
                }

            }
        }

        if (evaluate) {
            evaluateResult()
        }

        if (inputText.text.toString() == "0" && resultText.text.toString() == "=0") {
            resultText.visibility = View.GONE
        }
    }

    private fun enterInput(input: String) {
        val resultText = findViewById<android.widget.TextView>(R.id.result_text)
        if ((resultText.text.toString() == "=Overflow" || resultText.text.toString() == "=Underflow") && buttons[input] == "digit") {
            return
        }
        val inputText = findViewById<android.widget.TextView>(R.id.input_text)
        if (inputText.text.toString() == "0" && buttons[input] != "operator") {
            inputText.text = input
        } else {
            val lastTyped = inputText.text.toString().last().toString()
            if (buttons[input] == "operator" && buttons[lastTyped] == "operator") {
                inputText.text = inputText.text.toString().substring(0, inputText.text.toString().length - 1) + input
            } else {
                inputText.append(input)
            }
        }
    }

    private fun evaluateResult() {
        val inputText = findViewById<android.widget.TextView>(R.id.input_text)
        val resultText = findViewById<android.widget.TextView>(R.id.result_text)

        val lastTyped = inputText.text.toString().last().toString()
        val expressionToCalculate = if (buttons[lastTyped] == "operator") inputText.text.toString().substring(0, inputText.text.toString().length - 1) else inputText.text.toString()
        val expression : Expression = ExpressionBuilder(expressionToCalculate).build()

        try {
            val eval = expression.evaluate()
            var result = if(eval % 1 == 0.0) eval.toInt() else eval
            if (result == MAX_VALUE) {
                result = "Overflow"
            } else if (result == MIN_VALUE) {
                result = "Underflow"
            }
            resultText.text = "=" + result.toString()
            resultText.visibility = View.VISIBLE
        } catch (e: ArithmeticException) {
            resultText.text = "=Error calculating result"
        }
    }

    private fun clearText(all: Boolean) {
        val inputText = findViewById<android.widget.TextView>(R.id.input_text)
        val resultText = findViewById<android.widget.TextView>(R.id.result_text)
        inputText.text = "0"
        if (all) {
            resultText.text = "0"
            resultText.visibility = View.GONE
        }
    }

    private fun changeMainText(main: android.widget.TextView, secondary: android.widget.TextView) {
        main.textSize = 64f
        main.setTextColor(resources.getColor(R.color.black, null))
        secondary.textSize = 32f
        secondary.setTextColor(resources.getColor(R.color.secondaryText, null))
    }

    fun changeActivity(view: View) {
        val intent = android.content.Intent(this, ExchangeActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}