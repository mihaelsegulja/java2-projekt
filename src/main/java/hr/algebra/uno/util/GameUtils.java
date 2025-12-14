package hr.algebra.uno.util;

import hr.algebra.uno.model.*;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    public static Node createCardNode(Card card, boolean faceUp) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(80, 120);
        cardPane.setCursor(Cursor.HAND);
        cardPane.setEffect(new DropShadow(10, javafx.scene.paint.Color.GRAY));

        Rectangle rect = new Rectangle(80, 120);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setStroke(javafx.scene.paint.Color.BLACK);

        if (faceUp) {
            rect.setFill(javafx.scene.paint.Color.WHITE);

            Rectangle innerRect = new Rectangle(70, 110);
            innerRect.setArcWidth(8);
            innerRect.setArcHeight(8);
            innerRect.setFill(GameUtils.getColorForCard(card.getColor()));

            String cardText = GameUtils.getCardDisplayText(card.getValue(), false);

            DropShadow textShadow = new DropShadow();
            textShadow.setRadius(1.0);
            textShadow.setOffsetX(1.0);
            textShadow.setOffsetY(1.0);
            textShadow.setColor(javafx.scene.paint.Color.BLACK);

            Label centerLabel = new Label(cardText);

            javafx.scene.paint.Color labelColor = javafx.scene.paint.Color.WHITE;

            centerLabel.setTextFill(labelColor);
            centerLabel.setEffect(textShadow);

            FontWeight weight = (card.getValue().ordinal() <= 9) ? FontWeight.EXTRA_BOLD : FontWeight.BOLD;
            int fontSize = (card.getValue().ordinal() <= 9) ? 36 : 24;
            centerLabel.setFont(Font.font("Arial", weight, fontSize));

            cardPane.getChildren().addAll(rect, innerRect, centerLabel);

            String cornerText = GameUtils.getCardDisplayText(card.getValue(), true);

            Label cornerLabelTL = new Label(cornerText);
            cornerLabelTL.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
            cornerLabelTL.setTextFill(labelColor);
            cornerLabelTL.setEffect(textShadow);
            StackPane.setAlignment(cornerLabelTL, Pos.TOP_LEFT);
            cornerLabelTL.setTranslateX(10);
            cornerLabelTL.setTranslateY(10);

            Label cornerLabelBR = new Label(cornerText);
            cornerLabelBR.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
            cornerLabelBR.setTextFill(labelColor);
            cornerLabelBR.setEffect(textShadow);
            StackPane.setAlignment(cornerLabelBR, Pos.BOTTOM_RIGHT);
            cornerLabelBR.setTranslateX(-10);
            cornerLabelBR.setTranslateY(-10);
            cornerLabelBR.setRotate(180);

            cardPane.getChildren().addAll(cornerLabelTL, cornerLabelBR);

        } else {
            rect.setFill(javafx.scene.paint.Color.WHITE);

            Rectangle innerRect = new Rectangle(70, 110);
            innerRect.setArcWidth(8);
            innerRect.setArcHeight(8);
            innerRect.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.DARKRED),
                    new Stop(1, javafx.scene.paint.Color.BLACK)
            ));

            Ellipse unoOval = new Ellipse(25, 15);
            unoOval.setFill(javafx.scene.paint.Color.WHITE);
            unoOval.setRotate(-20);

            Label unoText = new Label("UNO");
            unoText.setTextFill(javafx.scene.paint.Color.GOLDENROD);
            unoText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
            unoText.setRotate(-20);

            cardPane.getChildren().addAll(rect, innerRect, unoOval, unoText);
        }

        return cardPane;
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

    public static String getCardDisplayText(Value value, boolean isCornerText) {
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
            case Skip -> isCornerText ? "S" : "SKIP";
            case Reverse -> isCornerText ? "R" : "REV";
            case Draw_Two -> "+2";
            case Wild -> isCornerText ? "W" : "WILD";
            case Wild_Draw_Four -> isCornerText ? "+4" : "W+4";
        };
    }

    public static Color generateRandomColor() {
        return Color.values()[new Random().nextInt(Color.values().length)];
    }
}
