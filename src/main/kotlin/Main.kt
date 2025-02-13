import blackjack.Deck
import blackjack.HiLoCount
import blackjack.Shoe
import blackjack.runGame

/**
 * Our main method, because why not?
 */
fun main(args: Array<String>) {
    println("Hello, World!")

    val hilo = HiLoCount()

    println("Running game in a deck")
    val deck = Deck()
    runGame(deck, hilo)

    println("\n\nRunning game in a shoe")
    val shoe = Shoe(4)
    runGame(shoe, hilo)
}
