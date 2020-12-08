package blackjack

const val DEALER_HIT_LIMIT = 17

/**
 *
 */
enum class Suit {
    CLUBS, SPADES, HEARTS, DIAMONDS
}

/**
 *
 */
enum class Value(val value: Int) {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13)
}

/**
 * The types of card-counting systems
 */
enum class CountingSystem {
    HILO,
    HILOOPTION1,
    HILOOPTION2,
    KO,
    OMEGA2,
    RED7,
    HALVES,
    ZENCOUNT
}

interface ICardHolder {
    val cards: MutableList<Card>

    fun shuffle()

    fun getCard(): Card {
        if (cards.count() == 0)
            shuffle()

        return cards.removeAt(0)
    }
}

interface ICardCountingSystem {
    var count: Int

    fun addToCount(card: Card): Int
}

/**
 *
 */
data class Card(val suit: Suit, val value: Value)

data class Player(val name: String, var balance: Float, var hands: MutableList<Hand>)

class Hand {
    constructor ()

    constructor(firstCard: Card) {
        cards.add(firstCard)
    }

    val cards: MutableList<Card> = mutableListOf()

    val value: Int
        get() {
            var handValue = 0

            for (card in this.cards) {
                when (card.value) {
                    Value.KING,
                    Value.QUEEN,
                    Value.JACK,
                    Value.TEN -> handValue += 10
                    Value.TWO -> handValue += 2
                    Value.THREE -> handValue += 3
                    Value.FOUR -> handValue += 4
                    Value.FIVE -> handValue += 5
                    Value.SIX -> handValue += 6
                    Value.SEVEN -> handValue += 7
                    Value.EIGHT -> handValue += 8
                    Value.NINE -> handValue += 9
                    Value.ACE -> handValue += when {
                        (handValue + 11) > 21 -> 1
                        else -> 11
                    }
                }
            }

            return handValue
        }

    var bet: Float = 0F

    override fun toString(): String {
        return cards.joinToString(",")
    }
}

class HiLoCount : ICardCountingSystem {
    override var count: Int = 0

    override fun addToCount(card: Card): Int {
        when (card.value) {
            Value.TWO,
            Value.THREE,
            Value.FOUR,
            Value.FIVE,
            Value.SIX -> count += 1

            Value.TEN,
            Value.JACK,
            Value.QUEEN,
            Value.KING,
            Value.ACE -> count -= 1

            // 7 - 9 the count remains stable so do nothing
            else -> count += 0
        }

        return count
    }
}

/**
 *
 */
class Deck : ICardHolder {
    override val cards: MutableList<Card> = mutableListOf<Card>()

    init {
        enumValues<Suit>().forEach {
            for (value in Value.values()) {
                cards.add(Card(it, value))
            }
        }

        cards.shuffle()
    }

    override fun shuffle() {
        cards.clear()

        enumValues<Suit>().forEach {
            for (value in Value.values()) {
                cards.add(Card(it, value))
            }
        }

        cards.shuffle()
    }
}

class Shoe(val countOfDecks: Int = 4) : ICardHolder {
    override val cards: MutableList<Card> = mutableListOf<Card>()

    init {
        for (i in 0 until countOfDecks) {
            cards.addAll(Deck().cards)
        }

        cards.shuffle()
    }

    var shuffle_point: Int? = null
        set(value) {
            if (value == null)
                return

            if (value < 0 || value > countOfDecks * 52) {
                throw IllegalArgumentException("insertion point cannot be higher than the total number of cards in the shoe")
            } else {
                field = value
            }
        }

    override fun shuffle() {
        cards.clear()

        for (i in 0 until countOfDecks) {
            enumValues<Suit>().forEach {
                for (value in Value.values()) {
                    cards.add(Card(it, value))
                }
            }

            cards.shuffle()
        }
    }
}

fun showDealersHand(dealersHand: Hand) {
    println("The dealer shows a ${dealersHand.cards[0]}")
}

fun runGame(
    cardholder: ICardHolder,
    cardCountingSystem: ICardCountingSystem? = null
) {
    println("Running game")

    /*
    var count = 0

    for (i in 0 .. 25) {
        val card = cardholder.cards[i]

        if (cardCountingSystem != null)
            count = cardCountingSystem.addToCount(card)

        println("$card - $count")
    }

    println("Count is $count")
    */

    var winPayoutMultiplier = 1F
    var blackjackPayoutMultiplier = 3F / 2F
    var tableMinimum = 5.0F

    val player1 = Player("Player 1", 15.00F, mutableListOf())
    val dealersHand = Hand()

    gameRound@ for (round in 0 until 5) {
        if (player1.balance < tableMinimum) {
            println("You ran out of money!  Better luck next time!")
            break
        }

        // deal to the player
        var hand = Hand(cardholder.getCard())
        hand.bet = tableMinimum
        player1.balance -= tableMinimum

        cardCountingSystem?.addToCount(hand.cards[0])

        println("Your balance is ${player1.balance}. Count is ${cardCountingSystem?.count}")

        // deal to the dealer
        dealersHand.cards.add(cardholder.getCard())
        cardCountingSystem?.addToCount(dealersHand.cards[0])

        // deal next round
        hand.cards.add(cardholder.getCard())
        cardCountingSystem?.addToCount(hand.cards[1])
        dealersHand.cards.add(cardholder.getCard())
        cardCountingSystem?.addToCount(dealersHand.cards[1])

        // assign to the player
        player1.hands.add(hand)

        println("${player1.name}'s hand: $hand - Value is: ${hand.value}. Count is ${cardCountingSystem?.count}")
        showDealersHand(dealersHand)

        if (dealersHand.cards[0].value == Value.ACE) {
            when (dealersHand.cards[1].value) {
                Value.KING, Value.QUEEN, Value.JACK, Value.TEN -> {
                    // the dealer got blackjack. the player has already lost
                    println("Dealer got blackjack. Sorry bub, you lost this round.")
                    // skip to the next iteration
                    continue@gameRound
                }
                else -> {
                    // not blackjack
                    println("Dodge a bullet there friend. It's not blackjack.")
                }
            }
        }

        if (hand.value == 21 && hand.cards.any { c -> c.value == Value.ACE }) {
            player1.balance += hand.bet * blackjackPayoutMultiplier
            println("You got blackjack!  You are killing it tonight!  You now have ${player1.balance}.")
            continue@gameRound
        }

        playerLoop@ while (true) {
            println("What do you want to do ${player1.name}?")

            val command = readLine()!!.toLowerCase()

            when (command) {
                "stand" -> {
                    break@playerLoop
                }
                "hit" -> {
                    hand.cards.add(cardholder.getCard())
                    println("${player1.name}'s hand: $hand - Value is: ${hand.value}")

                    val playerVal = hand.value

                    if (playerVal > 21) {
                        println("Oooohhh, sorry pardner you done busted!")
                        break@playerLoop
                    }
                }
                "double" -> {
                    // if there isn't enough to double down, warn them
                    if (hand.bet * 2 > player1.balance) {
                        println("You don't have enough to double down")
                    } else {
                        // enough exists, double the bet and deal one card
                        player1.balance -= hand.bet
                        hand.bet *= 2
                        hand.cards.add(cardholder.getCard())
                        break@playerLoop
                    }
                }
                "quit" -> {
                    break@gameRound
                }
                else -> {
                    println("You're talking gibberish son! Hit or stand!")
                }
            }
        }

        if (hand.value <= 21) {
            while (dealersHand.value < DEALER_HIT_LIMIT) {
                dealersHand.cards.add(cardholder.getCard())
                println("Dealer hit. Dealer's hand: $dealersHand - Value is: ${hand.value}")
            }

            println("\nThe results:")
            println("${player1.name}'s hand: $hand - Value is: ${hand.value}")
            println("Dealer's hand: $dealersHand - Value is: ${dealersHand.value}")

            val playerVal = hand.value
            val dealerVal = dealersHand.value

            when {
                dealerVal > 21 -> {
                    println("Dealer busted! You won!")
                    // increment the player's money
                    player1.balance += hand.bet * winPayoutMultiplier
                }
                playerVal < dealerVal -> println("You lost! Try again!")
                playerVal == dealerVal -> {
                    println("You pushed!")
                    player1.balance += hand.bet
                }
                playerVal > dealerVal -> {
                    println("You won!  High roller over here!")
                    // increment the player's money
                    player1.balance += hand.bet * winPayoutMultiplier
                }
            }
        } else {
            println("You busted with this hand: $hand")
        }

        hand.cards.clear()
        dealersHand.cards.clear()
        println("Player now has ${player1.balance}")
        println("")
    }
}

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
