package com.athtech.mrcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Collections


class MainActivity : AppCompatActivity() {

    private lateinit var userInputTextView: TextView
    private lateinit var resultTextView: TextView

    private var firstNumber: MutableList<Any> = ArrayList()
    private var secondNumber: MutableList<Any> = ArrayList()
    private var lastOperator: String = ""
    private var hasResult: Boolean = false
    private var resultTemp: String = ""


    // OnCreate state
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    // On PostCreate state
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        userInputTextView = findViewById<TextView>(R.id.userInputTextView)
        resultTextView = findViewById<TextView>(R.id.resultsTextView)
    }


    // Handle click on any number or decimal/sign button
    //---------------------------------------------------
    fun handleNumberButtonClick(view: View) {
        if (view is Button) {
            // First or second number input check
            if (lastOperator == "") {
                firstNumber.add(view.text)
            }
            else {
                if (hasResult) {
                    userInputTextView.text = ""
                    firstNumber.clear()
                    secondNumber.clear()
                    firstNumber.add(view.text)
                    resultTextView.text = ""
                    lastOperator = ""
                    hasResult = false
                }
                else {
                    secondNumber.add(view.text)
                }
            }
            userInputTextView.append(view.text)
        }
    }


    // Handle click on 'C (Clear)' and " ⌫ (Backspace)"
    //---------------------------------------------------
    fun handleActionButtonClick(view: View) {
        if (view.id == R.id.btn_clear) {
            clearAll()
        }
        else if (view.id == R.id.btn_backspace) {
            backSpace()
        }
    }


    // Handle the click on an 'operator' button
    //---------------------------------------------------
    fun handleOperatorButtonClick(view: View) {

        // If either "+", "-", "x", "/" was clicked
        if (view.id == R.id.btn_addition || view.id == R.id.btn_subtraction ||
            view.id == R.id.btn_multiplication || view.id == R.id.btn_division ) {

            if (lastOperator != "" && !hasResult) {
                Snackbar.make(view, "Cannot add multiple operators", 2500).show()
                return
            }

            if (userInputTextView.length() == 0) {
                Snackbar.make(view, "Cannot add operator with no number", 2500).show()
                return
            }

            if (hasResult) {
                firstNumber.clear()
                secondNumber.clear()
                firstNumber = resultTemp.toMutableList() as MutableList<Any>
                userInputTextView.text = resultTextView.text
                resultTextView.text = ""
            }

            if (view is Button){
                lastOperator = setOperator(view.id)
                userInputTextView.append(lastOperator)
                hasResult = false
            }
        }

        // If "=" was clicked
        if (view.id == R.id.btn_equals) {

            if (lastOperator == "" || hasResult) {
                return
            }

            if (firstNumber.size == 0 || secondNumber.size == 0) {
                Snackbar.make(view, "Two numbers and an operator are needed", 2500).show()
                return
            }

            Collections.replaceAll(firstNumber, ",", ".")
            Collections.replaceAll(secondNumber, ",", ".")

            val firstNumberBigDecimal = firstNumber.joinToString(prefix = "", postfix = "", separator = "").toBigDecimal()
            val secondNumberBigDecimal = secondNumber.joinToString(prefix = "", postfix = "", separator = "").toBigDecimal()

            if (lastOperator == "/" && BigDecimal.ZERO.compareTo(secondNumberBigDecimal) == 0) {
                Snackbar.make(view, "Cannot divide by zero !", 2500).show()
                return
            }

            val result = calculate(firstNumberBigDecimal, secondNumberBigDecimal, lastOperator)

            resultTemp = result.toString()
            resultTextView.text = result.toPlainString().replace(".", ",")

            // Use regex to remove any decimal trailing zeros (.00000)
            resultTextView.text = resultTextView.text.replace(Regex("(\\,)0+\\b"), "")

            if (view is Button){
                hasResult = true
                userInputTextView.append("=")
            }
        }
    }


    // Perform calculations between two numbers
    private fun calculate(num1 :BigDecimal, num2: BigDecimal, operator: String): BigDecimal {
        var res: BigDecimal

        if (operator == "+")
            res = num1 + num2
        else if (operator == "-")
            res = num1 - num2
        else if (operator == "x")
            res = num1 * num2
        else
            res = num1.divide(num2, 7, RoundingMode.HALF_UP)

        return res.stripTrailingZeros()
    }


    // Set the operator based on the button's id that was clicked
    private fun setOperator(id: Int) : String {
        var oper = ""

        if (id == R.id.btn_addition) {
            oper = "+"
        }
        else if (id == R.id.btn_subtraction) {
            oper = "-"
        }
        else if (id == R.id.btn_multiplication) {
            oper = "x"
        }
        else if (id == R.id.btn_division) {
            oper = "/"
        }

        return oper
    }


    // Clear everything on the calculator
    private fun clearAll() {
        userInputTextView.text = ""
        resultTextView.text = ""
        lastOperator = ""
        hasResult = false
        firstNumber.clear()
        secondNumber.clear()
    }


    // Delete the last character in the user input textview
    private fun backSpace() {
        val length = userInputTextView.length()

        if (length > 0) {

            if (userInputTextView.text.endsWith(lastOperator) && lastOperator != "") {
                lastOperator = ""
            }
            else if (userInputTextView.text.endsWith("=")) {
                resultTextView.text = ""
                hasResult = false
            }
            else {
                if (lastOperator == "") {
                    firstNumber.removeLast()
                }
                else {
                    secondNumber.removeLast()
                }
            }
            userInputTextView.text = userInputTextView.text.subSequence(0, length - 1)
        }
    }
}