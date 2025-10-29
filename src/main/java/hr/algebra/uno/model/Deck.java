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

    public void initializeStandardUnoDeck() {
        drawPile.clear();
        discardPile.clear();

        // Add colored cards
        for (Color color : List.of(Color.Red, Color.Green, Color.Blue, Color.Yellow)) {

            // One ZERO per color
            drawPile.add(new Card(color, Value.Zero));

            // Two of each 1–9 per color
            for (Value v : List.of(Value.One, Value.Two, Value.Three, Value.Four, Value.Five,
                    Value.Six, Value.Seven, Value.Eight, Value.Nine)) {
                drawPile.add(new Card(color, v));
                drawPile.add(new Card(color, v));
            }

            // Two of each action card per color
            for (Value v : List.of(Value.Skip, Value.Reverse, Value.Draw_Two)) {
                drawPile.add(new Card(color, v));
                drawPile.add(new Card(color, v));
            }
        }

        // Add Wild and Wild Draw Four cards (no color)
        for (int i = 0; i < 4; i++) {
            drawPile.add(new Card(Color.Wild, Value.Wild));
            drawPile.add(new Card(Color.Wild, Value.Wild_Draw_Four));
        }

        // Shuffle
        shuffle();
    }
}
