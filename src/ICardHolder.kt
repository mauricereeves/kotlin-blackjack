package blackjack

interface ICardHolder {
    val cards: MutableList<Card>

    fun shuffle()

    fun getCard(): Card {
        if (cards.count() == 0)
            shuffle()

        return cards.removeAt(0)
    }
}