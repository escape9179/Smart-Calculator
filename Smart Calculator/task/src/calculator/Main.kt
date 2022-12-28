
package calculator

import java.util.Scanner
import kotlin.system.exitProcess

const val BYE_MESSAGE = "Bye!"
const val NUMBER_DELIMITER = ' '

fun main() {
    val numberList = mutableListOf<Int>()
    val scanner = Scanner(System.`in`)
    /* Keep reading input until the user enters a command or a new line is entered. */
    while (scanner.hasNextLine()) {
        val input = scanner.nextLine()
        /* Process whichever command the user entered
        * then exit input processing. */
        if (input in Command.labels()) {
            processCommand(input)
            return
        }
        /* Request input again if a blank line is entered. */
        if (input.isBlank()) {
            continue
        }
        /* Split input into integers */
        val inputNumbers = input.split(NUMBER_DELIMITER).map { it.toInt() }
        /* Add the number entered to a list of numbers to be
        * added together. */
        numberList.addAll(inputNumbers)
        println(numberList.sum())
        numberList.clear()
    }
}

fun processCommand(input: String) {
    when (input) {
        Command.EXIT.label -> {
            println(BYE_MESSAGE)
            exitProcess(0)
        }
    }
}

enum class Command(val label: String) {
    EXIT("/exit");

    companion object {
        fun labels(): List<String> {
            return Command.values().map { it.label }
        }
    }
}
