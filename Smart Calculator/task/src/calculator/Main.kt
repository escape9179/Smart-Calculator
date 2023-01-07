package calculator

import java.lang.NullPointerException
import java.math.BigInteger
import java.util.LinkedList
import java.util.Scanner
import java.util.Stack
import kotlin.system.exitProcess

private const val OPERATOR_PLACEHOLDER = "!"
private const val NUMBER_PLACEHOLDER = "_"
private const val ERROR = "error"
private const val INVALID_EXPRESSION_ERROR = "Invalid expression"
private const val VARIABLE_REGEX = "[A-Za-z]+"
private val variableMap = mutableMapOf<String, BigInteger>()
private const val INVALID_ASSIGNMENT_ERROR = "Invalid assignment"
private const val UNKNOWN_VARIABLE_ERROR = "Unknown variable"
private const val INVALID_IDENTIFIER_ERROR = "Invalid identifier"
private const val BYE_MESSAGE = "Bye!"
private const val INPUT_DELIMITER = ' '
private const val PLUS_SIGN = "+"
private const val MINUS_SIGN = "-"
private const val MULTIPLY_SIGN = "*"
private const val DIVIDE_SIGN = "/"
private const val COMMAND_PREFIX = "/"
private const val EQUALS_SIGN = "="
private const val VARIABLE_ASSIGNMENT_REGEX = "$VARIABLE_REGEX *= *-?([0-9]+|[A-Za-z]+) *$"

fun main() {
    val scanner = Scanner(System.`in`)
    /* Keep reading input until the user enters a command or a new line is entered. */
    while (scanner.hasNextLine()) {
        var input = scanner.nextLine().trim()
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
            println(INVALID_ASSIGNMENT_ERROR)
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
            println(INVALID_IDENTIFIER_ERROR)
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
                    println(UNKNOWN_VARIABLE_ERROR)
                    continue
                }
            }
            /* Assume that the expression on the right side of the equation
            * is an integer since the variable regex couldn't match it and
            * assign the variable that integer. */
            variableMap[lhs] = rhs.toBigInteger()
            continue
        } else if (EQUALS_SIGN in input) {
            println(INVALID_ASSIGNMENT_ERROR)
            continue
        }
        if (!isValidExpression(input)) {
            println(INVALID_IDENTIFIER_ERROR)
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
            println(UNKNOWN_VARIABLE_ERROR)
            continue
        }

        if (input.matches(Regex("-*\\+*\\d+"))) {
            println(input.replace("+", "").replace(" ", ""))
            continue
        }

        /* Checks if there are un-even parenthesis. */
        val rightParenthesisCount = input.count { it == '(' }
        val leftParenthesisCount = input.count { it == ')' }
        if (rightParenthesisCount != leftParenthesisCount) {
            println(INVALID_EXPRESSION_ERROR)
            continue
        }

        /* Replace plus signs preceding minus signs with minus signs,
        * and minus signs preceding plus signs with plus signs. */
        input=input.replace(Regex("\\++\\s*-+\\s*"), "-")
            .replace(Regex("-+\\s*\\++\\s*"), "+")

        val postfix = convertToPostfix(input)
        if(postfix == INVALID_ASSIGNMENT_ERROR|| postfix==INVALID_EXPRESSION_ERROR|| postfix==INVALID_IDENTIFIER_ERROR){
            println(postfix)
            continue
        }


        /* Postfix to result. */
        val answer = convertPostfixToAnswer(postfix)
        println(answer)
    }

}

fun convertPostfixToAnswer(postfix: String): BigInteger {
    val stack = LinkedList<String>()
    for (element in postfix.split(" ")) {
        /* If the incoming element is a number, push it into the stack (the whole number, not a single digit!). */
        if (element.isNumber()) {
            stack.push(element)
            continue
        }
        // TODO: If the incoming element is the name of a variable, push its value into the stack????
        /* If the incoming element is an operator, then pop twice to get two numbers and perform the operation;
        * push the result on the stack. */
        if (element.isOperator()) {
            val num1=stack.pop().toBigInteger()
            val num2=stack.pop().toBigInteger()
            when (Operator.getOperand(element)) {
                Operator.PLUS -> stack.push((num1 + num2).toString())
                Operator.MINUS -> stack.push((num2 - num1).toString())
                Operator.ASTERISK -> stack.push((num1 * num2).toString())
                Operator.FORWARD_SLASH -> stack.push((num2/num1).toString())
                Operator.HAT->stack.push(Math.pow(num2.toDouble(), num1.toDouble()).toInt().toString())
            }
        }
    }
    return stack.pop().toBigInteger()
}

private fun convertToPostfix(input: String): String {
    /**
     * A list containing all the numbers in the input.
     */
    val numbers = input.split(Regex("[ +\\-*/()^]+")).filter { it.isNotBlank() }

    /**
     * A queue of all numbers in the input expression.
     */
    val numberQueue = LinkedList(numbers.map { it.toBigInteger() })


    /**
     * The input expression with all numbers replaced by underscores. This
     * makes it easier when converting infix to postfix.
     */
    val inputWithoutNumbersOrSpacesOrOperators = input.replace(Regex("\\d+"), NUMBER_PLACEHOLDER)
        .replace(Regex("[+\\-*/^]+"), OPERATOR_PLACEHOLDER)
        .filter { it != ' ' }
        .toCharArray().map { it.toString() }

    val operators = input.split(Regex("[\\d ()]+")).filter { it.isNotBlank() }.map {
        if(it.length%2==0&&it[0]== MINUS_SIGN[0]) PLUS_SIGN
        else if(it.length>1&&(it[0]==Operator.ASTERISK.symbol[0]||it[0]==Operator.FORWARD_SLASH.symbol[0])) return INVALID_EXPRESSION_ERROR
        else it[0].toString()
    }
    val operatorQueue = LinkedList(operators)

    /**
     * A list containing operands and operators. After the infix to postfix conversion
     * the list will be joined into a string giving the postfix expression as a result.
     */
    val operatorOperandList = mutableListOf<Any>()

    /**
     * The stack used for storing operands and operators during the conversion process.
     */
    val stack = Stack<String>()

    for (char in inputWithoutNumbersOrSpacesOrOperators) {
        /* The underscores represent numbers. If an underscore is encountered, append
        * that number to the postfix result and remove it from the number queue. */
        if (char.isUnderscore()) {
            operatorOperandList.add(numberQueue.remove())
            continue
        }
        if (char.isExclamationMark()) {
            val operator = operatorQueue.remove()
            /* If the stack is empty or contains a left parenthesis on top,
            * push the incoming operator on the stack. */
            if (stack.empty() || stack.peek() == Operator.LEFT_PARENTHESIS.symbol) {
                stack.push(operator)
                continue
            }
            /* If the incoming operator has higher precedence than the top of the stack, push it on the stack. */
            if (Operator.getOperand(operator).precedence > Operator.getOperand(stack.peek()).precedence) {
                stack.push(operator)
                continue
            }
            /* If the precedence of the incoming operator is lower than or equal to that of the top of the stack,
            * pop the stack and add operators to the result until you see an operator that has smaller
            * precedence or a left parenthesis on the top of the stack; then add the incoming operator to the stack. */
            if (Operator.getOperand(operator).precedence <= Operator.getOperand(stack.peek()).precedence) {
                do {
                    operatorOperandList.add(stack.pop())
                } while (stack.isNotEmpty() && (Operator.getOperand(stack.peek()).precedence > Operator.getOperand(operator).precedence || stack.peek() != Operator.LEFT_PARENTHESIS.symbol)
                )
                stack.push(operator)
                continue
            }
        }
        if(char.isParenthesis()) {
            /* If the incoming element is a left parenthesis, push it on the stack. */
            if (char == Operator.LEFT_PARENTHESIS.symbol) {
                stack.push(char)
                continue
            }
            /* If the incoming element is a right parenthesis, pop the stack and add operators until you see a left
            * parenthesis. Discard the pair of parenthesis. */
            if (char == Operator.RIGHT_PARENTHESIS.symbol) {
                do {
                    if(stack.isEmpty())return INVALID_EXPRESSION_ERROR
                    operatorOperandList.add(stack.pop())
                } while (stack.isNotEmpty() && stack.peek() != Operator.LEFT_PARENTHESIS.symbol)
                /* Discard the left parenthesis */
                if (stack.isNotEmpty()) stack.pop()
                continue
            }
        }
    }
    /* If any parenthesis remain on the stack then it's a syntax error. */
    if (Operator.LEFT_PARENTHESIS.symbol in stack || Operator.RIGHT_PARENTHESIS.symbol in stack) {
        return INVALID_EXPRESSION_ERROR
    }
    /* At the end of the expression, pop the stack and add all operators to the result. */
    while (stack.isNotEmpty()) {
        operatorOperandList.add(stack.pop())
    }
    return operatorOperandList.joinToString(" ")
}

private fun String.isExclamationMark(): Boolean {
    return Regex("!").matches(this)
}

private fun String.isParenthesis(): Boolean {
    return Regex("[()]").matches(this)
}

private fun String.isUnderscore(): Boolean {
    return Regex("_").matches(this)
}

private fun String.isOperator(): Boolean {
    return Regex("[+\\-*/^]+").matches(this)
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
        || input.startsWith(Operator.HAT.symbol)
        || input.endsWith(Operator.HAT.symbol)
    ) {
        return false
    }
    if ("\\d+ \\d+|[a-zA-Z]+\\d+".toRegex().containsMatchIn(input)) {
        return false
    }
    return true
}

enum class Operator(val symbol: String, val precedence: Int) {
    NONE(" ", 0),
    PLUS("+", 1),
    MINUS("-", 1),
    ASTERISK("*", 2),
    FORWARD_SLASH("/", 2),
    LEFT_PARENTHESIS("(", 0),
    RIGHT_PARENTHESIS(")", 0),
    HAT("^",3);


    companion object {
        fun getOperand(s: String): Operator {
            if (s.length > 1) {
                throw NullPointerException()
            }
            return values().first { it.symbol == s }
        }
    }
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