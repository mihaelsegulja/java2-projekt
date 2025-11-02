package hr.algebra.uno.engine;

import hr.algebra.uno.model.*;
import hr.algebra.uno.util.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private GameState gameState;

    public GameEngine() {}

    public GameEngine(GameState state) {
        this.gameState = state;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void startNewGame(List<String> playerNames) {
        Deck deck = createStandardDeck();
        List<Player> players = playerNames.stream().map(Player::new).toList();

        for (Player p : players) {
            List<Card> dealt = deck.drawCards(7);
            p.addCards(dealt);
        }

        // pick a valid starting discard (not wild draw four)
        Card first;
        do {
            if (deck.getDrawPile().isEmpty()) {
                deck.reshuffleFromDiscardPile();
            }

            first = deck.drawCard();

            if (first.getValue() == Value.Wild_Draw_Four) {
                deck.returnToBottom(first);
                first = null; // force another draw
            }
        } while (first == null);

        deck.discard(first);

        // create game state and set defaults
        GameState state = new GameState();
        state.setPlayers(players);
        state.setDeck(deck);
        state.setCurrentPlayerIndex(0);
        state.setClockwise(true);
        state.setGameOver(false);

        this.gameState = state;
    }

    public void playCard(Player player, Card card) {
        Card topCard = gameState.getDeck().peekTopCard();
        if (!isValidMove(card, topCard)) {
            return;
        }

        player.removeCard(card);

        gameState.getDeck().discard(card);

        applyCardEffect(card);

        if (player.getHand().isEmpty()) {
            gameState.setGameOver(true);
            return;
        }

        nextTurn();
    }

    public void drawCard(Player player) {
        Card drawn = gameState.getDeck().drawCard();

        // if deck runs out, reshuffle discard pile except top
        if (drawn == null) {
            gameState.getDeck().reshuffleFromDiscardPile();
            drawn = gameState.getDeck().drawCard();
        }

        player.addCard(drawn);
    }

    public void nextTurn() {
        int playerCount = gameState.getPlayers().size();
        int current = gameState.getCurrentPlayerIndex();

        if (gameState.isClockwise()) {
            gameState.setCurrentPlayerIndex((current + 1) % playerCount);
        } else {
            gameState.setCurrentPlayerIndex((current - 1 + playerCount) % playerCount);
        }
    }

    public boolean isValidMove(Card played, Card topCard) {
        return played.getColor() == topCard.getColor()
                || played.getValue() == topCard.getValue()
                || played.getColor() == Color.Wild;
    }

    public void applyCardEffect(Card card) {
        switch (card.getValue()) {
            case Skip -> nextTurn();
            case Reverse -> gameState.setClockwise(!gameState.isClockwise());
            case Draw_Two -> {
                Player next = getNextPlayer();
                next.addCards(gameState.getDeck().drawCards(2));
                nextTurn();
            }
            case Wild_Draw_Four -> {
                Player next = getNextPlayer();
                next.addCards(gameState.getDeck().drawCards(4));
                nextTurn();
            }
            default -> {}
        }
    }

    public Player getNextPlayer() {
        List<Player> players = gameState.getPlayers();
        if (players == null || players.isEmpty()) return null;
        int playerCount = players.size();
        int current = gameState.getCurrentPlayerIndex();
        int nextIndex = gameState.isClockwise()
                ? (current + 1) % playerCount
                : (current - 1 + playerCount) % playerCount;
        return players.get(nextIndex);
    }

    public static Deck createStandardDeck() {
        Deck deck = new Deck();
        deck.initializeStandardUnoDeck();
        return deck;
    }
}
