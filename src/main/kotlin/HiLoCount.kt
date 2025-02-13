package blackjack

class HiLoCount : ICardCountingSystem {
    override var count: Int = 0

    override fun addToCount(card: Card): Int {
        when (card.value) {
            Value.TWO,
            Value.THREE,
            Value.FOUR,
            Value.FIVE,
            Value.SIX,
            -> count += 1

            Value.TEN,
            Value.JACK,
            Value.QUEEN,
            Value.KING,
            Value.ACE,
            -> count -= 1

            // 7 - 9 the count remains stable so do nothing
            else -> count += 0
        }

        return count
    }
}
