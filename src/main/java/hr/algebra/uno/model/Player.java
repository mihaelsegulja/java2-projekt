package hr.algebra.uno.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private List<Card> hand = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            addCard(cards.get(i));
        }
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void removeCard(int index) {
        hand.remove(index);
    }
}
