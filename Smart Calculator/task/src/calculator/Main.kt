package calculator

import java.util.Scanner
import kotlin.system.exitProcess

const val BYE_MESSAGE = "Bye!"
const val INPUT_DELIMITER = ' '
const val MINUS_SIGN = '-'

fun main() {
    val numberList = mutableListOf<Int>()
    val scanner = Scanner(System.`in`)
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
        var currentOperation = Operation.NONE
        var total = 0
        /* Iterate through each token and when a token starts with a
        * dash for subtraction, count the number of dashes to determine
        * whether it should be interpreted as addition or subtraction.
        * Don't do anything special for any other types of operators. */
        for (token in tokens) {
            if (isOperatorString(token)) {
                if (token.first() == Operation.SUBTRACTION.operand) {
                    currentOperation = if (token.isEvenLength()) Operation.ADDITION else Operation.SUBTRACTION
                    continue
                }
                currentOperation = Operation.valueOf(token.first())
                continue
            }
            /* Modify the total depending on the operand (if one has been reached). */
            when (currentOperation) {
                /* When an operand hasn't been read yet
                * assume the token is a number and set the total to that
                * number. */
                Operation.NONE -> total = token.toInt()
                Operation.ADDITION -> total += token.toInt()
                Operation.SUBTRACTION -> total -= token.toInt()
                Operation.MULTIPLICATION -> total *= token.toInt()
                Operation.DIVISION -> total /= token.toInt()
            }
        }
        println(total)
    }
}

enum class Operation(val operand: Char) {
    NONE(' '), ADDITION('+'), SUBTRACTION('-'), MULTIPLICATION('*'), DIVISION('%');

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
 * @param token The string being checked
 * @return True if the string contains one or many of one type of operand.
 * False otherwise
 */
fun isOperatorString(token: String): Boolean {
    return Operation.operators().any { opChar -> opChar in token && token.all { c -> opChar == c } }
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
