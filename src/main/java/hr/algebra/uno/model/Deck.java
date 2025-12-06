package hr.algebra.uno.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck implements java.io.Serializable {
    @Getter
    private final Stack<Card> drawPile = new Stack<>();
    @Getter
    private final Stack<Card> discardPile = new Stack<>();

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    public Card drawCard() {
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    public List<Card> drawCards(int n) {
        List<Card> cards = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Card c = drawCard();
            if (c == null) break;
            cards.add(c);
        }
        return cards;
    }

    public void returnToBottom(Card card) {
        drawPile.insertElementAt(card, 0);
    }

    public void discardCard(Card card) {
        discardPile.push(card);
    }

    public Card peekTopCard() {
        return discardPile.isEmpty() ? null : discardPile.peek();
    }

    public void initializeStandardUnoDeck() {
        reset();

        // Add colored cards
        for (Color color : List.of(Color.Red, Color.Green, Color.Blue, Color.Yellow)) {

            // One ZERO per color
            drawPile.add(new Card(color, Value.Zero));

            // Two of each 1–9 per color
            for (Value value : List.of(Value.One, Value.Two, Value.Three, Value.Four, Value.Five,
                    Value.Six, Value.Seven, Value.Eight, Value.Nine)) {
                drawPile.add(new Card(color, value));
                drawPile.add(new Card(color, value));
            }

            // Two of each action card per color
            for (Value value : List.of(Value.Skip, Value.Reverse, Value.Draw_Two)) {
                drawPile.add(new Card(color, value));
                drawPile.add(new Card(color, value));
            }
        }

        // Add Wild and Wild Draw Four cards (no color)
        for (int i = 0; i < 4; i++) {
            drawPile.add(new Card(Color.Wild, Value.Wild));
            drawPile.add(new Card(Color.Wild, Value.Wild_Draw_Four));
        }

        shuffle();
    }

    public void reshuffleFromDiscardPile() {
        if (discardPile.size() <= 1) return;

        Card top = discardPile.pop(); // Keep the top discardCard
        List<Card> toReshuffle = new ArrayList<>(discardPile);
        discardPile.clear();

        // Put top card back
        discardPile.push(top);

        Collections.shuffle(toReshuffle);
        drawPile.addAll(toReshuffle);
    }

    public void reset() {
        drawPile.clear();
        discardPile.clear();
    }
}
