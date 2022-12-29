package calculator

import java.util.Scanner
import kotlin.system.exitProcess

const val BYE_MESSAGE = "Bye!"
const val INPUT_DELIMITER = ' '

fun main() {
    val numberList = mutableListOf<Int>()
    val scanner = Scanner(System.`in`)
    var lastOperator: String
    /* Keep reading input until the user enters a command or a new line is entered. */
    while (scanner.hasNextLine()) {
        val input = scanner.nextLine()
        /* Process whichever command the user entered
        * then go back to input processing. */
        if (input in Command.labels()) {
            processCommand(input)
            continue
        }
        /* Request input again if a blank line is entered. */
        if (input.isBlank()) {
            continue
        }
        /* Split input string into tokens (separated by spaces)
         * and evaluate the expression. */
        val tokens = input.split(INPUT_DELIMITER)
        var currentOp: Operation
        var result: Int
        /* Iterate through each token and when a token starts with a
        * dash for subtraction, count the number of dashes to determine
        * whether it should be interpreted as addition or subtraction.
        * Don't do anything special for any other types of operators. */
        for (token in tokens) {
            if (isOperatorString(token)) {
                if (token.first() == Operation.SUBTRACTION.operand) {
                    currentOp = if (token.isEvenLength()) Operation.ADDITION else Operation.SUBTRACTION
                    continue
                }
                currentOp = Operation.valueOf(token.first())
                continue
            }

        }
        /* Add the number entered to a list of numbers to be
        * added together. */
        numberList.addAll(inputNumbers)
        println(numberList.sum())

    }
}

enum class Operation(val operand: Char) {
    ADDITION('+'), SUBTRACTION('-'), MULTIPLICATION('*'), DIVISION('%');

    companion object {
        fun operators(): List<Char> {
            return values().map { it.operand }
        }

        fun valueOf(c: Char): Operation {
            return values().first { it.operand == c }
        }
    }
}

/**
 * Checks if a string is an even length.
 * @return True if the string is an even length. False otherwise.
 */
private fun String.isEvenLength(): Boolean {
    return length % 2 == 0
}

/**
 * Checks if a string contains one or many of one type of operand.
 * @param s The string being checked
 * @return True if the string contains one or many of one type of operand.
 * False otherwise
 */
fun isOperatorString(s: String): Boolean {
    return Operation.operators().any { opChar -> opChar in s && s.all { c -> c in s}}
}

fun processCommand(input: String) {
    when (input) {
        Command.EXIT.label -> {
            println(BYE_MESSAGE)
            exitProcess(0)
        }

        Command.HELP.label -> {
            println("This program calculates the sum of numbers.")
        }
    }
}

enum class Command(val label: String) {
    EXIT("/exit"),
    HELP("/help");

    companion object {
        fun labels(): List<String> {
            return Command.values().map { it.label }
        }
    }
}
