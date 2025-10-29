package hr.algebra.uno.util;

import hr.algebra.uno.model.Deck;
import hr.algebra.uno.model.GameState;
import hr.algebra.uno.model.Player;

import java.io.*;
import java.util.List;

public class GameUtils {
    private static final String SAVE_PATH = "game/save.dat";

    public static void saveGame(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            oos.writeObject(state);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save game", e);
        }
    }

    public static GameState loadGame() {
        GameState gameState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_PATH))) {
            gameState = (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load game", e);
        }

        return gameState;
    }

    private List<Player> createPlayers(List<String> names) {
        return names.stream().map(Player::new).toList();
    }

    private Deck initializeDeck() {
        Deck deck = new Deck();
        deck.initializeStandardUnoDeck();
        deck.shuffle();
        return deck;
    }
}
