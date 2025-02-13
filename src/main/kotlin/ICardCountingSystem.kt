package blackjack

interface ICardCountingSystem {
    var count: Int

    fun addToCount(card: Card): Int
}
