package hr.algebra.uno.controller;

import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.jndi.ConfigurationKey;
import hr.algebra.uno.jndi.ConfigurationReader;
import hr.algebra.uno.model.*;
import hr.algebra.uno.network.NetworkManager;
import hr.algebra.uno.rmi.ChatRemoteService;
import hr.algebra.uno.util.ChatUtils;
import hr.algebra.uno.util.DialogUtils;
import hr.algebra.uno.util.DocumentationUtils;
import hr.algebra.uno.util.GameUtils;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static hr.algebra.uno.UnoApplication.playerType;

public class GameController {
    @FXML public TextField tfChat;
    @FXML public Button btnChat;
    @FXML public TextArea taChat;
    @FXML private HBox hbPlayerHand;
    @FXML private HBox hbOpponentHand;
    @FXML private StackPane spDrawPile;
    @FXML private StackPane spDiscardPile;
    @FXML private Label lbStatus;
    @FXML private Button btnUno;

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private GameEngine gameEngine = new GameEngine();
    private NetworkManager networkManager;
    private static final int PLAYER_1_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_1_SERVER_PORT);
    private static final int PLAYER_2_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_2_SERVER_PORT);
    public static boolean gameInitialized = false;
    ChatRemoteService chatRemoteService;

    public void initialize() {
        if (playerType != PlayerType.Singleplayer) {
            if (playerType == PlayerType.Player_1) {
                networkManager = new NetworkManager(playerType, PLAYER_1_PORT, PLAYER_2_PORT, this);
                networkManager.startServer(gameEngine);
            } else if (playerType == PlayerType.Player_2) {
                networkManager = new NetworkManager(playerType, PLAYER_2_PORT, PLAYER_1_PORT, this);
                networkManager.startServer(gameEngine);
            }
        }

        Optional<ChatRemoteService> chatRemoteServiceOptional = ChatUtils.initializeChatRemoteService();
        chatRemoteServiceOptional.ifPresent(remoteService -> chatRemoteService = remoteService);

        Timeline chatMessagesRefreshTimeLine = ChatUtils.getChatRefreshTimeline(chatRemoteService, taChat);
        chatMessagesRefreshTimeLine.play();
    }

    public void startNewGame() {
        if (playerType == PlayerType.Player_1) {
            gameEngine.startNewGame(List.of(
                    new Player(PlayerType.Player_1.toString(), "Player 1"),
                    new Player(PlayerType.Player_2.toString(), "Player 2")));
            networkManager.sendGameState(gameEngine.getGameState());
            renderGameState();
            gameInitialized = true;
        } else if (playerType == PlayerType.Player_2) {
            lbStatus.setText("Waiting for Player 1 to start the game...");
        }
    }

    public void renderGameState() {
        GameState state = gameEngine.getGameState();
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();

        if (state.isGameOver()) {
            DialogUtils.showWinnerDialog(state.getWinnerName());
            disableGameUI();
            return;
        }

        String currPlayer = state.getCurrentPlayer().getName();
        lbStatus.setText(currPlayer + "'s turn");

        int localIndex = resolveLocalPlayerIndex(state);
        Player me = state.getPlayers().get(localIndex);

        log.info("Current turn: {}", state.getCurrentPlayer().getName());
        log.info("Local player resolved as: {}", me.getName());

        btnUno.setVisible(me.isMustCallUno() && !me.isUnoCalled());
        btnUno.setDisable(!(me.isMustCallUno() && !me.isUnoCalled()));

        for (Card card : me.getHand()) {
            Node cardNode = GameUtils.createCardNode(card, true);

            if (state.getCurrentPlayerIndex() == localIndex) {
                cardNode.setOnMouseClicked(e -> handleCardClick(card));
                cardNode.setCursor(Cursor.HAND);
            } else {
                cardNode.setDisable(true);
                cardNode.setOpacity(0.6);
            }

            hbPlayerHand.getChildren().add(cardNode);
        }

        int opponentIndex = -1;
        for (int i = 0; i < state.getPlayers().size(); i++) {
            if (i != localIndex) {
                opponentIndex = i;
                break;
            }
        }

        int opponentCardCount = state.getPlayers().get(opponentIndex).getHand().size();
        for (int i = 0; i < opponentCardCount; i++) {
            hbOpponentHand.getChildren().add(GameUtils.createCardNode(null, false));
        }

        Card topDiscard = state.getDeck().peekTopCard();
        if (topDiscard != null) {
            spDiscardPile.getChildren().add(GameUtils.createCardNode(topDiscard, true));
        }

        Node drawPileNode = GameUtils.createCardNode(null, false);

        if (state.getCurrentPlayerIndex() == localIndex) {
            drawPileNode.setOnMouseClicked(e -> handleDrawCardClick());
            drawPileNode.setCursor(Cursor.HAND);
        } else {
            drawPileNode.setOpacity(0.6);
            drawPileNode.setOnMouseClicked(null);
        }

        spDrawPile.getChildren().add(drawPileNode);
    }

    private void disableGameUI() {
        hbPlayerHand.setDisable(true);
        spDrawPile.setDisable(true);
        btnUno.setDisable(true);
    }

    private void handleCardClick(Card card) {
        GameState gameState = gameEngine.getGameState();
        int localIndex = resolveLocalPlayerIndex(gameState);
        Player current = gameState.getPlayers().get(localIndex);
        if (card.getColor() == Color.Wild) {
            Color chosenColor = DialogUtils.showColorPickerDialog();
            gameEngine.playCard(current, card, chosenColor);
        } else {
            gameEngine.playCard(current, card, null);
        }
        networkManager.sendGameState(gameEngine.getGameState());
        renderGameState();
    }

    private void handleDrawCardClick() {
        GameState gameState = gameEngine.getGameState();
        int localIndex = resolveLocalPlayerIndex(gameState);
        Player current = gameState.getPlayers().get(localIndex);
        lbStatus.setText(current.getName() + "'s turn");
        gameEngine.drawCard(current);
        networkManager.sendGameState(gameEngine.getGameState());
        renderGameState();
    }

    public void saveGame() {
        GameUtils.saveGame(gameEngine.getGameState());
    }

    public void loadGame() {
        GameState loaded = GameUtils.loadGame();
        gameEngine.setGameState(loaded);
        networkManager.sendGameState(loaded);
        renderGameState();
    }

    public void generateDocumentation() {
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

    public void sendChatMessage() {
        ChatUtils.sendChatMessage(chatRemoteService, tfChat);
    }

    public void handleKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            sendChatMessage();
            keyEvent.consume();
        }
    }

    public void handleUnoClick() {
        GameState gameState = gameEngine.getGameState();
        int localIndex = resolveLocalPlayerIndex(gameState);
        Player me = gameState.getPlayers().get(localIndex);

        if (me.isMustCallUno() && !me.isUnoCalled()) {
            gameEngine.callUno(me);
            btnUno.setVisible(false);

            if (networkManager != null) {
                networkManager.sendGameState(gameEngine.getGameState());
            }
        }
    }

    private int resolveLocalPlayerIndex(GameState state) {
        for (int i = 0; i < state.getPlayers().size(); i++) {
            if (state.getPlayers().get(i).getId().equals(playerType.toString())) {
                return i;
            }
        }
        throw new IllegalStateException("Local player not found in GameState");
    }
}
