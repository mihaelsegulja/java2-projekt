package hr.algebra.uno.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck {
    private final Stack<Card> drawPile = new Stack<>();
    private final Stack<Card> discardPile = new Stack<>();

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    public Card drawCard() {
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    public List<Card> drawCards(int n) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cards.add(drawCard());
        }
        return cards;
    }

    public void discard(Card card) {
        discardPile.push(card);
    }

    public Card peekTopCard() {
        return discardPile.isEmpty() ? null : discardPile.peek();
    }
}
