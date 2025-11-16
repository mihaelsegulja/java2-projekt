package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Player implements java.io.Serializable {
    private final String name;
    private final List<Card> hand = new ArrayList<>();

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        for (Card card : cards) {
            addCard(card);
        }
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void removeCard(int index) {
        hand.remove(index);
    }
}
