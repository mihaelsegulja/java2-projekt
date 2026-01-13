package hr.algebra.uno.util;

import hr.algebra.uno.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class GameUtils {
    private static final Logger log = LoggerFactory.getLogger(GameUtils.class);
    private static final String SAVE_PATH = "game/save.dat";

    private GameUtils() {}

    public static void saveGame(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            Path path = Path.of(SAVE_PATH);
            Files.createDirectories(path.getParent());
            oos.writeObject(state);
        } catch (IOException e) {
            log.error("Error while saving game", e);
        }
    }

    public static GameState loadGame() {
        File file = new File(SAVE_PATH);
        if (!file.exists()) {
            throw new RuntimeException("Save file not found at: " + SAVE_PATH);
        }

        GameState gameState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_PATH))) {
            gameState = (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load game", e);
        }

        return gameState;
    }

    public static Color generateRandomColor() {
        return Color.values()[new Random().nextInt(Color.values().length)];
    }
}
