package hr.algebra.uno.util;

import hr.algebra.uno.model.*;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

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

    public static Paint getColorForCard(Color color) {
        return switch (color) {
            case Red -> new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.RED),
                    new Stop(1, javafx.scene.paint.Color.MAROON)
            );
            case Green -> new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.LIMEGREEN),
                    new Stop(1, javafx.scene.paint.Color.FORESTGREEN)
            );
            case Blue -> new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.BLUE),
                    new Stop(1, javafx.scene.paint.Color.DARKBLUE)
            );
            case Yellow -> new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.YELLOW),
                    new Stop(1, javafx.scene.paint.Color.GOLDENROD)
            );
            case Wild -> new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.BLACK),
                    new Stop(1, javafx.scene.paint.Color.DIMGRAY)
            );
        };
    }

    public static String getCardDisplayText(Value value) {
        return switch (value) {
            case Zero -> "0";
            case One -> "1";
            case Two -> "2";
            case Three -> "3";
            case Four -> "4";
            case Five -> "5";
            case Six -> "6";
            case Seven -> "7";
            case Eight -> "8";
            case Nine -> "9";
            case Skip -> "SKIP";
            case Reverse -> "REV";
            case Draw_Two -> "+2";
            case Wild -> "WILD";
            case Wild_Draw_Four -> "W+4";
        };
    }

    public static String getCardCornerText(Value value) {
        return switch (value) {
            case Zero -> "0";
            case One -> "1";
            case Two -> "2";
            case Three -> "3";
            case Four -> "4";
            case Five -> "5";
            case Six -> "6";
            case Seven -> "7";
            case Eight -> "8";
            case Nine -> "9";
            case Skip -> "S";
            case Reverse -> "R";
            case Draw_Two -> "+2";
            case Wild -> "W";
            case Wild_Draw_Four -> "+4";
        };
    }
}
