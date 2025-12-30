package hr.algebra.uno.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck implements Serializable {
    @Getter
    private final Stack<Card> drawPile = new Stack<>();
    @Getter
    private final Stack<Card> discardPile = new Stack<>();

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    public Card drawCard() {
        if (drawPile.isEmpty()) {
            reshuffleFromDiscardPile();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    public List<Card> drawCards(int n) {
        List<Card> cards = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Card card = drawCard();
            if (card == null) break;
            cards.add(card);
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
        for (Color color : List.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)) {

            // ONE ZERO per color
            drawPile.add(new Card(color, Value.ZERO));

            // TWO of each 1–9 per color
            for (Value value : List.of(Value.ONE, Value.TWO, Value.THREE, Value.FOUR, Value.FIVE,
                    Value.SIX, Value.SEVEN, Value.EIGHT, Value.NINE)) {
                drawPile.add(new Card(color, value));
                drawPile.add(new Card(color, value));
            }

            // TWO of each action card per color
            for (Value value : List.of(Value.SKIP, Value.REVERSE, Value.DRAW_TWO)) {
                drawPile.add(new Card(color, value));
                drawPile.add(new Card(color, value));
            }
        }

        // Add WILD and WILD Draw FOUR cards (no color)
        for (int i = 0; i < 4; i++) {
            drawPile.add(new Card(Color.WILD, Value.WILD));
            drawPile.add(new Card(Color.WILD, Value.WILD_DRAW_FOUR));
        }

        shuffle();
    }

    public void reshuffleFromDiscardPile() {
        if (discardPile.size() <= 1) return;

        Card top = discardPile.pop();
        drawPile.addAll(discardPile);
        discardPile.clear();
        discardPile.push(top);

        Collections.shuffle(drawPile);
    }

    public void reset() {
        drawPile.clear();
        discardPile.clear();
    }
}
