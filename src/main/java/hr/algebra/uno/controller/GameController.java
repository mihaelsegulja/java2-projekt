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
import javafx.scene.paint.*;
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
                localPlayerIndex = playerType.getIndex();
                networkManager = new NetworkManager(playerType, Player_1_Port, Player_2_Port, this);
                networkManager.startServer(gameEngine);
            } else if (playerType == PlayerType.Player_2) {
                localPlayerIndex = playerType.getIndex();
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

    public void renderGameState() {
        GameState state = gameEngine.getGameState();
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();

        // Player hand (face-up)
        for (Card card : state.getPlayers().get(localPlayerIndex).getHand()) {
            Node cardNode = GameUtils.createCardNode(card, true);

            if (state.getCurrentPlayerIndex() == localPlayerIndex) {
                cardNode.setOnMouseClicked(e -> handleCardClick(card));
                cardNode.setCursor(Cursor.HAND);
            } else {
                cardNode.setDisable(true);
                cardNode.setOpacity(0.6);
            }

            hbPlayerHand.getChildren().add(cardNode);
        }

        // Opponent hand (back only)
        int opponentCardCount = state.getPlayers().get(opponentPlayerIndex()).getHand().size();
        for (int i = 0; i < opponentCardCount; i++) {
            hbOpponentHand.getChildren().add(GameUtils.createCardNode(null, false));
        }

        // Discard pile
        Card topDiscard = state.getDeck().peekTopCard();
        if (topDiscard != null) {
            spDiscardPile.getChildren().add(GameUtils.createCardNode(topDiscard, true));
        }

        // Draw pile
        Node drawPileNode = GameUtils.createCardNode(null, false);

        if (state.getCurrentPlayerIndex() == localPlayerIndex) {
            drawPileNode.setOnMouseClicked(e -> handleDrawCardClick());
            drawPileNode.setCursor(Cursor.HAND);
        } else {
            drawPileNode.setOpacity(0.6);
            drawPileNode.setOnMouseClicked(null);
        }

        spDrawPile.getChildren().add(drawPileNode);
    }

    private void handleCardClick(Card card) {
        GameState gameState = gameEngine.getGameState();
        Player current = gameState.getPlayers().get(localPlayerIndex);
        if (gameState.getCurrentPlayerIndex() != localPlayerIndex) {
            lbStatus.setText("Wait for your turn...");
            return;
        }
        gameEngine.playCard(current, card);
        renderGameState();
        networkManager.sendGameState(gameEngine.getGameState());
    }

    private void handleDrawCardClick() {
        GameState gameState = gameEngine.getGameState();
        Player current = gameState.getPlayers().get(localPlayerIndex);
        lbStatus.setText(current.getName() + "'s turn");
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
