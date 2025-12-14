package hr.algebra.uno.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Player implements Serializable {
    private final String id;
    private final String name;
    private final List<Card> hand = new ArrayList<>();

    @Setter
    private boolean mustCallUno;
    @Setter
    private boolean unoCalled;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public void resetUno() {
        mustCallUno = false;
        unoCalled = false;
    }
}
