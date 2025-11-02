package hr.algebra.uno.controller;

import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.model.*;
import hr.algebra.uno.network.NetworkManager;
import hr.algebra.uno.util.DialogUtils;
import hr.algebra.uno.util.DocumentationUtils;
import hr.algebra.uno.util.GameUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static hr.algebra.uno.UnoApplication.playerType;

public class GameController {
    @FXML private HBox hbPlayerHand;
    @FXML private HBox hbOpponentHand;
    @FXML private StackPane spDrawPile;
    @FXML private StackPane spDiscardPile;
    @FXML private Label lbStatus;

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private GameEngine gameEngine = new GameEngine();
    private NetworkManager networkManager;
    private static final int Player_1_Port = 9001;
    private static final int Player_2_Port = 9002;

    public void startNewGame() {
        List<String> playerNames;
        if (playerType == PlayerType.Singleplayer) {
            playerNames = List.of("Player", "Bot");
        }
        else {
            playerNames = List.of("Player 2", "Player 1");
        }

        gameEngine.startNewGame(playerNames);

        if (playerType != PlayerType.Singleplayer) {
            if (playerType == PlayerType.Player_1) {
                networkManager = new NetworkManager(playerType, Player_1_Port, Player_2_Port);
                networkManager.startServer(gameEngine);
            } else if (playerType == PlayerType.Player_2) {
                networkManager = new NetworkManager(playerType, Player_2_Port, Player_1_Port);
                networkManager.startServer(gameEngine);
            }
        }

        renderGameState();
    }

    private Node createCardNode(Card card, boolean faceUp) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(80, 120);
        cardPane.setCursor(Cursor.HAND);

        Rectangle rect = new Rectangle(80, 120);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setStroke(javafx.scene.paint.Color.BLACK);

        if (faceUp) {
            rect.setFill(getColorForCard(card.getColor()));

            Label label = new Label(card.getValue().toString());
            label.setTextFill(javafx.scene.paint.Color.WHITE);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            cardPane.getChildren().addAll(rect, label);
        } else {
            rect.setFill(new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.DARKRED),
                    new Stop(1, javafx.scene.paint.Color.BLACK)
            ));

            Label unoText = new Label("UNO");
            unoText.setTextFill(javafx.scene.paint.Color.WHITE);
            unoText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            unoText.setRotate(-20);

            cardPane.getChildren().addAll(rect, unoText);
        }

        return cardPane;
    }

    private Paint getColorForCard(Color color) {
        return switch (color) {
            case Red -> javafx.scene.paint.Color.CRIMSON;
            case Green -> javafx.scene.paint.Color.FORESTGREEN;
            case Blue -> javafx.scene.paint.Color.ROYALBLUE;
            case Yellow -> javafx.scene.paint.Color.GOLD;
            case Wild -> javafx.scene.paint.Color.DARKGRAY;
        };
    }

    private void renderGameState() {
        GameState state = gameEngine.getGameState();
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();

        // Player hand (face-up)
        for (Card card : state.getPlayers().get(0).getHand()) {
            hbPlayerHand.getChildren().add(createCardNode(card, true));
        }

        // Opponent hand (back only)
        int opponentCardCount = state.getPlayers().get(1).getHand().size();
        for (int i = 0; i < opponentCardCount; i++) {
            hbOpponentHand.getChildren().add(createCardNode(null, false));
        }

        // Discard pile (face-up)
        Card topDiscard = state.getDeck().peekTopCard();
        if (topDiscard != null) {
            spDiscardPile.getChildren().add(createCardNode(topDiscard, true));
        }

        // Draw pile (back)
        spDrawPile.getChildren().add(createCardNode(null, false));
        spDrawPile.setOnMouseClicked(e -> handleDrawCardClick());
    }


    private void handleCardClick(Card card) {
        Player current = gameEngine.getGameState().getPlayers().get(0);
        gameEngine.playCard(current, card);
        renderGameState();
    }

    private void handleDrawCardClick() {
        Player current = gameEngine.getGameState().getPlayers().get(0);
        gameEngine.drawCard(current);
        renderGameState();
    }

    public void saveGame() {
        GameUtils.saveGame(gameEngine.getGameState());
    }

    public void loadGame() {
        GameState loaded = GameUtils.loadGame();
        gameEngine = new GameEngine(loaded);
    }

    public void generateDocumentation(ActionEvent actionEvent) {
        try {
            DocumentationUtils.generateDocumentationHtmlFile();
            DialogUtils.showDialog("Success",
                    "HTML docs successfully created!",
                    Alert.AlertType.INFORMATION);
            log.info("Documentation file generated.");
        } catch (IOException e) {
            DialogUtils.showDialog("Error",
                    "Something went wrong while generating HTML docs.",
                    Alert.AlertType.ERROR);
            log.error("Documentation file failed to generate.", e);
        }
    }
}
