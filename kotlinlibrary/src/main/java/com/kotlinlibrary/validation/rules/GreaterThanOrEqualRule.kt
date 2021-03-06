package com.kotlinlibrary.validation.rules

import com.kotlinlibrary.validation.datatype.validNumber
import java.text.NumberFormat

class GreaterThanOrEqualRule(val target: Number, var errorMsg: String = "Should be greater than or equal to $target") : BaseRule {

    override fun validate(text: String): Boolean {
        if (text.isEmpty())
            return false

        // Negative
        if (text.startsWith("-")) {
            val txtNum = text.substringAfter("-")
            if (txtNum.validNumber()) {
                var number = NumberFormat.getNumberInstance().parse(txtNum)
                number = number.toFloat() * -1
                return (number.toFloat() >= target.toFloat())
            }
            return false
        }
        // Positive
        else {
            if (text.validNumber()) {
                val number = NumberFormat.getNumberInstance().parse(text)
                return (number.toFloat() >= target.toFloat())
            }
            return false
        }
    }

    override fun getErrorMessage(): String = errorMsg

    override fun setError(msg: String) {
        errorMsg = msg
    }
}