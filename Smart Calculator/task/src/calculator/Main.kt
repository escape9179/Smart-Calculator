package calculator

import java.util.Scanner
import kotlin.system.exitProcess

private const val INVALID_INPUT_MESSAGE = "Invalid expression"
private const val BYE_MESSAGE = "Bye!"
private const val INPUT_DELIMITER = ' '
private const val PLUS_SIGN = "+"
private const val MINUS_SIGN = "-"
private const val MULTIPLY_SIGN = "*"
private const val DIVIDE_SIGN = "/"
private const val COMMAND_PREFIX = "/"


fun main() {
    val scanner = Scanner(System.`in`)
    /* Keep reading input until the user enters a command or a new line is entered. */
    while (scanner.hasNextLine()) {
        val input = scanner.nextLine()
        /* Process whichever command the user entered
        * then go back to input processing. */
        if (input.startsWith(COMMAND_PREFIX)) {
            processCommand(input)
            continue
        }
        /* Request input again if a blank line is entered. */
        if (input.isBlank()) {
            continue
        }
        if (!isValidExpression(input)) {
            println(INVALID_INPUT_MESSAGE)
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

private fun isValidExpression(input: String): Boolean {
    /* If the input ends with an +, -, *, or / then the expression is invalid. */
    if (input.endsWith(PLUS_SIGN)
        || input.endsWith(MINUS_SIGN)
        || input.startsWith(MULTIPLY_SIGN)
        || input.startsWith(DIVIDE_SIGN)
    ) {
        return false
    }
    /* The first alternation of the regex checks if there are two numbers separated by a space
    * and the second alternation checks if there are any latin characters.
    * Both alternations shouldn't be present in the string and therefore return false if present. */
    if ("(\\d+ \\d+|[A-Za-z]+)".toRegex().containsMatchIn(input)) return false
    return true
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
        else -> {
            println("Unknown command")
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