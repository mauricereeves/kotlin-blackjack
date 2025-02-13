import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals

import blackjack.Card
import blackjack.Suit
import blackjack.Value
import blackjack.Hand

class GameTests {
    @Test
    fun `test hand value`() {
        val king = Card(Suit.HEARTS, Value.KING)
        val ace = Card(Suit.HEARTS, Value.ACE)
        val hand = Hand()
        hand.cards.add(king)
        hand.cards.add(ace)
        assertEquals(21, hand.value)
    }
}