package hr.algebra.uno.controller;

import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.jndi.ConfigurationKey;
import hr.algebra.uno.jndi.ConfigurationReader;
import hr.algebra.uno.model.*;
import hr.algebra.uno.model.Color;
import hr.algebra.uno.network.NetworkManager;
import hr.algebra.uno.rmi.ChatRemoteService;
import hr.algebra.uno.rmi.RmiServer;
import hr.algebra.uno.util.DialogUtils;
import hr.algebra.uno.util.DocumentationUtils;
import hr.algebra.uno.util.GameUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

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
    private int localPlayerIndex;
    public static boolean gameInitialized = false;
    ChatRemoteService chatRemoteService;

    public void initialize() {
        if (playerType != PlayerType.Singleplayer) {
            if (playerType == PlayerType.Player_1) {
                localPlayerIndex = playerType.getIndex();
                networkManager = new NetworkManager(playerType, PLAYER_1_PORT, PLAYER_2_PORT, this);
                networkManager.startServer(gameEngine);
            } else if (playerType == PlayerType.Player_2) {
                localPlayerIndex = playerType.getIndex();
                networkManager = new NetworkManager(playerType, PLAYER_2_PORT, PLAYER_1_PORT, this);
                networkManager.startServer(gameEngine);
            }
        }

        try {
            Registry registry = LocateRegistry.getRegistry(RmiServer.HOSTNAME, RmiServer.RMI_PORT);
            chatRemoteService = (ChatRemoteService) registry.lookup(ChatRemoteService.REMOTE_OBJECT_NAME);
        } catch (RemoteException | NotBoundException e) {
            log.error("RMI error", e);
        }

        Timeline chatMessagesRefreshTimeLine = getChatRefreshTimeline();
        chatMessagesRefreshTimeLine.play();
    }

    private Timeline getChatRefreshTimeline() {
        Timeline chatMessagesRefreshTimeLine = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                try {
                    List<String> chatMessages = chatRemoteService.getAllMessages();

                    StringBuilder textMessagesBuilder = new StringBuilder();

                    for(String message : chatMessages) {
                        textMessagesBuilder.append(message).append("\n");
                    }

                    taChat.setText(textMessagesBuilder.toString());

                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }), new KeyFrame(Duration.seconds(1)));
        chatMessagesRefreshTimeLine.setCycleCount(Animation.INDEFINITE);
        return chatMessagesRefreshTimeLine;
    }

    public void startNewGame() {
        if (playerType == PlayerType.Player_1) {
            gameEngine.startNewGame(List.of("Player 1", "Player 2"));
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

        String currPlayer = state.getCurrentPlayer().getName();
        lbStatus.setText(currPlayer + "'s turn");

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

        int opponentCardCount = state.getPlayers().get(opponentPlayerIndex()).getHand().size();
        for (int i = 0; i < opponentCardCount; i++) {
            hbOpponentHand.getChildren().add(GameUtils.createCardNode(null, false));
        }

        Card topDiscard = state.getDeck().peekTopCard();
        if (topDiscard != null) {
            spDiscardPile.getChildren().add(GameUtils.createCardNode(topDiscard, true));
        }

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
        if (card.getColor() == Color.Wild) {
            Color chosenColor = DialogUtils.showColorPickerDialog();
            gameEngine.playCard(current, card, chosenColor);
        } else {
            gameEngine.playCard(current, card, null);
        }
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
        localPlayerIndex = playerType.getIndex();
        gameInitialized = true;
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
        String chatMessage = tfChat.getText();
        if(chatMessage.isBlank()) return;
        tfChat.clear();
        try {
            chatRemoteService.sendChatMessage(playerType + ": " + chatMessage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            sendChatMessage();
            keyEvent.consume();
        }
    }

    public void handleUnoClick() {

    }
}
