package hr.algebra.uno.controller;

import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.model.*;
import hr.algebra.uno.network.NetworkManager;
import hr.algebra.uno.util.DialogUtils;
import hr.algebra.uno.util.DocumentationUtils;
import hr.algebra.uno.util.GameUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Ellipse;
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
    private int localPlayerIndex;

    public void initialize() {
        if (playerType != PlayerType.Singleplayer) {
            if (playerType == PlayerType.Player_1) {
                localPlayerIndex = 0;
                networkManager = new NetworkManager(playerType, Player_1_Port, Player_2_Port, this);
                networkManager.startServer(gameEngine);
            } else if (playerType == PlayerType.Player_2) {
                localPlayerIndex = 1;
                networkManager = new NetworkManager(playerType, Player_2_Port, Player_1_Port, this);
                networkManager.startServer(gameEngine);
            }
        }
    }

    public void startNewGame() {
        if (playerType == PlayerType.Player_1) {
            gameEngine.startNewGame(List.of("Player 1", "Player 2"));
            networkManager.sendGameState(gameEngine.getGameState());
            renderGameState();
        } else if (playerType == PlayerType.Player_2) {
            gameEngine.startNewGame(List.of("Player 2", "Player 1"));
        }
    }

    private Node createCardNode(Card card, boolean faceUp) {
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

            String cardText = GameUtils.getCardDisplayText(card.getValue());

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

            String cornerText = GameUtils.getCardCornerText(card.getValue());

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

    public void renderGameState() {
        GameState state = gameEngine.getGameState();
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();

        // Player hand (face-up)
        for (Card card : state.getPlayers().get(localPlayerIndex).getHand()) {
            hbPlayerHand.getChildren().add(createCardNode(card, true));
            hbPlayerHand.setOnMouseClicked(e -> handleCardClick(card));
        }

        // Opponent hand (back only)
        int opponentCardCount = state.getPlayers().get(opponentPlayerIndex()).getHand().size();
        for (int j = 0; j < opponentCardCount; j++) {
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
        GameState gameState = gameEngine.getGameState();
        Player current = gameState.getPlayers().get(localPlayerIndex);
        if (gameEngine.getGameState().getCurrentPlayerIndex() != 0) {
            lbStatus.setText("Wait for your turn...");
            return;
        }
        gameEngine.playCard(current, card);
        renderGameState();
        networkManager.sendGameState(gameEngine.getGameState());
    }

    private void handleDrawCardClick() {
        GameState gameState = gameEngine.getGameState();
        Player current = gameEngine.getGameState().getPlayers().get(localPlayerIndex);
        if (gameEngine.getGameState().getCurrentPlayerIndex() != 0) {
            lbStatus.setText("Wait for your turn...");
            return;
        }
        gameEngine.drawCard(current);
        renderGameState();
        networkManager.sendGameState(gameEngine.getGameState());
    }

    private int opponentPlayerIndex() {
        return localPlayerIndex == 0 ? 1 : 0;
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
