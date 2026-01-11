package hr.algebra.uno.engine;

import hr.algebra.uno.config.GameConfig;
import hr.algebra.uno.model.*;
import hr.algebra.uno.util.GameUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
public class GameEngine {
    @Getter @Setter
    private GameState gameState;
    @Getter @Setter
    private GameConfig config;

    public GameEngine(GameConfig config) {
        this.config = config;
    }

    public GameEngine(GameState state) {
        this.gameState = state;
    }

    public void startNewGame(List<Player> players) {
        Deck deck = createStandardDeck();

        for (Player p : players) {
            List<Card> dealt = deck.drawCards(config.getStartingCards());
            p.addCards(dealt);
        }

        if (deck.getDrawPile().isEmpty()) {
            deck.reshuffleFromDiscardPile();
        }

        Card first = deck.drawCard();

        if (first.getValue() == Value.WILD_DRAW_FOUR || first.getValue() == Value.WILD) {
            first.setWildColor(GameUtils.generateRandomColor());
        }

        deck.discardCard(first);

        GameState state = new GameState();
        state.setPlayers(players);
        state.setDeck(deck);
        state.setCurrentPlayerIndex(new Random().nextInt(players.size()));
        state.setClockwise(true);
        state.setGameOver(false);

        this.gameState = state;
    }

    public void playCard(Player player, Card card, Color chosenColor) {
        Card topCard = gameState.getDeck().peekTopCard();
        if (!isValidMove(card, topCard)) return;

        if (chosenColor != null) {
            card.setWildColor(chosenColor);
        }

        player.removeCard(card);
        gameState.getDeck().discardCard(card);

        if (player.getHand().size() == 1) {
            player.setMustCallUno(true);
            player.setUnoCalled(false);
        } else {
            player.resetUno();
        }

        applyCardEffect(card);

        if (player.getHand().isEmpty()) {
            gameState.setGameOver(true);
            gameState.setWinnerName(player.getName());
            return;
        }

        nextTurn();
    }

    public void drawCard(Player player) {
        Card drawn = gameState.getDeck().drawCard();
        if (drawn != null){
            player.addCard(drawn);
            player.resetUno();
        }
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
                || played.getColor() == Color.WILD;
    }

    public void applyCardEffect(Card card) {
        switch (card.getValue()) {
            case SKIP -> nextTurn();
            case REVERSE -> {
                gameState.setClockwise(!gameState.isClockwise());
                nextTurn();
            }
            case DRAW_TWO -> {
                Player next = getNextPlayer();
                next.addCards(gameState.getDeck().drawCards(2));
            }
            case WILD_DRAW_FOUR -> {
                Player next = getNextPlayer();
                next.addCards(gameState.getDeck().drawCards(4));
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

    public void callUno(Player player) {
        if (player.isMustCallUno() && !player.isUnoCalled()) {
            player.setUnoCalled(true);
        }
    }

    public boolean isPlayersTurn(String playerId) {
        return gameState.getCurrentPlayer().getId().equals(playerId);
    }

    public Player getPlayerById(String playerId) {
        return gameState.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found: " + playerId));
    }
}
