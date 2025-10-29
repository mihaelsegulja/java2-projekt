package hr.algebra.uno.engine;

import hr.algebra.uno.model.Card;
import hr.algebra.uno.model.GameState;
import hr.algebra.uno.model.Player;

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

    public void startNewGame() {
        gameState = new GameState();
    }

    public void playCard(Player player, Card card) {

    }

    public void drawCard(Player player) {

    }

    public void nextTurn() {

    }

    public boolean isValidMove(Card played, Card topCard) {

        return true;
    }

    public void applyCardEffect(Card card) {

    }
}
