package calculator

import java.lang.NullPointerException
import java.util.LinkedList
import java.util.Queue
import java.util.Scanner
import java.util.Stack
import kotlin.system.exitProcess

private const val RIGHT_PARENTHESIS = ")"
private const val LEFT_PARENTHESIS = "("
private const val VARIABLE_REGEX = "[A-Za-z]+"
private val variableMap = mutableMapOf<String, Int>()
private const val INVALID_ASSIGNMENT_MESSAGE = "Invalid assignment"
private const val UNKNOWN_VARIABLE_MESSAGE = "Unknown variable"
private const val INVALID_IDENTIFIER_MESSAGE = "Invalid identifier"
private const val BYE_MESSAGE = "Bye!"
private const val INPUT_DELIMITER = ' '
private const val PLUS_SIGN = "+"
private const val MINUS_SIGN = "-"
private const val MULTIPLY_SIGN = "*"
private const val DIVIDE_SIGN = "/"
private const val COMMAND_PREFIX = "/"
private const val EQUALS_SIGN = "="
private const val VARIABLE_ASSIGNMENT_REGEX = "$VARIABLE_REGEX *= *([0-9]+|[A-Za-z]+) *$"

fun main() {
    val scanner = Scanner(System.`in`)
    /* Keep reading input until the user enters a command or a new line is entered. */
    while (scanner.hasNextLine()) {
        var input = scanner.nextLine()
        /* Process whichever command the user entered
        * then go back to input processing. */
        if (input.startsWith(COMMAND_PREFIX)) {
            processCommand(input)
            continue
        }
        /* Do nothing and request input again if a blank line is entered. */
        if (input.isBlank()) {
            continue
        }

        if (EQUALS_SIGN.toRegex().findAll(input).count() > 1) {
            println(INVALID_ASSIGNMENT_MESSAGE)
            continue
        }

        var parts: List<String> // Parts of the equation separated by '='
        var lhs = "" // The left-hand side of the equation
        var rhs = ""// The right-hand side of the equation
        if (EQUALS_SIGN in input) {
            parts = input.split(EQUALS_SIGN).map { it.trim() }
            lhs = parts[0]
            rhs = parts[1]
        }

        if (lhs.contains("([A-Za-z]+\\d+|\\d+[A-Za-z]+)".toRegex())) {
            println(INVALID_IDENTIFIER_MESSAGE)
            continue
        }
        /* Check if the input is a variable assignment. If it is, then
        * split the string at the '=' and put the variable and value in
        * the variable map. */
        if (VARIABLE_ASSIGNMENT_REGEX.toRegex().matches(input)) {
            /* If the value being assigned to the variable is also a variable
            * and that same variable is also in the variable map, then assign
            * the variable on the left side of the equation the value of the
            * variable in the right. */
            if ("[A-Za-z]+".toRegex().matches(rhs)) {
                if (rhs in variableMap) {
                    variableMap[lhs] = variableMap[rhs]!!
                    continue
                } else {
                    /* If the variable on the right side of the expression
                    * isn't in the variable map, then print an error message. */
                    println(UNKNOWN_VARIABLE_MESSAGE)
                    continue
                }
            }
            /* Assume that the expression on the right side of the equation
            * is an integer since the variable regex couldn't match it and
            * assign the variable that integer. */
            variableMap[lhs] = rhs.toInt()
            continue
        } else if (EQUALS_SIGN in input) {
            println(INVALID_ASSIGNMENT_MESSAGE)
            continue
        }
        if (!isValidExpression(input)) {
            println(INVALID_IDENTIFIER_MESSAGE)
            continue
        }
        /* Replace all variables in the expression with their numerical values,
        * if they are present within the variable map. If the variable isn't present
        * send an error message. */
        var hasUnknownVariable = false
        "\\b[a-zA-Z]+\\b".toRegex().findAll(input).forEach {
            if (variableMap[it.value] == null) {
                hasUnknownVariable = true
                return@forEach
            }
            input = input.replace(it.value, variableMap[it.value].toString())
        }
        if (hasUnknownVariable) {
            println(UNKNOWN_VARIABLE_MESSAGE)
            continue
        }

        val postfix = convertToPostfix(input)
        println("postfixResult = $postfix")
    }

}

private fun convertToPostfix(input: String): String {
    /* Find all numbers within the input and replace them with underscores. */
    val numbers = input.split(Regex("[ +\\-*/()]+")).filter { it.isNotBlank() }
    val numberQueue = LinkedList(numbers.map { it.toInt() })
    val inputWithoutNumbers = input.replace(Regex("\\d+"), "_").toCharArray().map { it.toString() }
    val resultCharList = mutableListOf<Any>()
    val stack = Stack<String>()
    for (char in inputWithoutNumbers) {
/*
        if (char.isBlank()) {
            postfixResult.append(' ')
            continue
        }
*/
        /* The underscores represent numbers. If an underscore is encountered, append
        * that number to the postfix result and remove it from the number queue. */
        if (char.isUnderscore()) {
            resultCharList.add(numberQueue.remove())
            continue
        }
        if (char.isOperator()) {
            /* If the stack is empty or contains a left parenthesis on top,
                * push the incoming operator on the stack. */
            if (stack.empty() || stack.peek() == LEFT_PARENTHESIS) {
                stack.push(char)
                continue
            }
            /* If the incoming operand has higher precedence than the top of the stack, push it on the stack. */
            if (Operation.getOperand(char).priority > Operation.getOperand(stack.peek()).priority) {
                stack.push(char)
                continue
                /* If the precedence of the incoming operator is lower than or equal to that of the top of the stack,
                    * pop the stack and add operators to the result until you see an operator that has smaller
                    * precedence or a left parenthesis on the top of the stack; then add the incoming operator to the stack. */
            }
            if (Operation.getOperand(char).priority <= Operation.getOperand(stack.peek()).priority) {
                do {
                    resultCharList.add(stack.pop())
                } while (!stack.empty() && (stack.peek() != LEFT_PARENTHESIS || Operation.getOperand(stack.peek()) >= Operation.getOperand(
                        char
                    ))
                )
                stack.push(char)
                continue
            }
            continue
        }
        /* If the incoming element is a left parenthesis, push it on the stack. */
        if (char == LEFT_PARENTHESIS) {
            stack.push(char)
            continue
        }
        /* If the incoming element is a right parenthesis, pop the stack and add operators until you see a left
            * parenthesis. Discard the pair of parenthesis. */
        if (char == RIGHT_PARENTHESIS) {
            do {
                resultCharList.add(stack.pop())
            } while (stack.peek() != LEFT_PARENTHESIS)
            /* Discard the left parenthesis */
            stack.pop()
            continue
        }
    }
    /* At the end of the expression, pop the stack and add all operators to the result. */
    while (!stack.empty()) {
        resultCharList.add(stack.pop())
    }
    return resultCharList.joinToString(" ")
}

private fun String.isUnderscore(): Boolean {
    return Regex("_").matches(this)
}

private fun String.isOperator(): Boolean {
    return Regex("[+\\-*/]+").matches(this)
}

private fun String.isNumber(): Boolean {
    return "\\d+".toRegex().matches(this)
}

/**
 *
 */
private fun isValidExpression(input: String): Boolean {
    /* If the input ends with an +, -, *, or / then the expression is invalid. */
    if (input.endsWith(PLUS_SIGN)
        || input.endsWith(MINUS_SIGN)
        || input.startsWith(MULTIPLY_SIGN)
        || input.startsWith(DIVIDE_SIGN)
    ) {
        return false
    }
    if ("\\d+ \\d+|[a-zA-Z]+\\d+".toRegex().containsMatchIn(input)) {
        return false
    }
    return true
}

enum class Operation(val operand: Char, val priority: Int) {
    NONE(' ', 0), ADDITION('+', 1), SUBTRACTION('-', 1), MULTIPLICATION('*', 2), DIVISION('%', 2);

    companion object {
        fun operators(): List<Char> {
            return values().map { it.operand }
        }

        fun getOperand(c: Char): Operation {
            return values().first { it.operand == c }
        }

        fun getOperand(s: String): Operation {
            if (s.length > 1) {
                throw NullPointerException()
            }
            return getOperand(s[0])
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