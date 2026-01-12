package hr.algebra.uno.controller;

import hr.algebra.uno.config.GameConfig;
import hr.algebra.uno.config.GameConfigParser;
import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.jndi.ConfigurationKey;
import hr.algebra.uno.jndi.ConfigurationReader;
import hr.algebra.uno.model.*;
import hr.algebra.uno.network.NetworkManager;
import hr.algebra.uno.rmi.ChatRemoteService;
import hr.algebra.uno.util.*;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import java.util.concurrent.ThreadLocalRandom;

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
    private GameConfig config;
    private GameEngine gameEngine;
    private NetworkManager networkManager;
    private static final int PLAYER_1_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_1_SERVER_PORT);
    private static final int PLAYER_2_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_2_SERVER_PORT);
    ChatRemoteService chatRemoteService;

    public void initialize() {
        config = GameConfigParser.load();
        gameEngine = new GameEngine(config);

        switch (playerType) {
            case PLAYER_1 -> {
                networkManager = new NetworkManager(playerType, PLAYER_1_PORT, PLAYER_2_PORT, this);
                networkManager.startServer(gameEngine);
            }
            case PLAYER_2 -> {
                networkManager = new NetworkManager(playerType, PLAYER_2_PORT, PLAYER_1_PORT, this);
                networkManager.startServer(gameEngine);
            }
            case SINGLEPLAYER -> {
                log.info("SINGLEPLAYER");
            }
            default -> log.error("Provided player type not valid.");
        }

        Optional<ChatRemoteService> chatRemoteServiceOptional = ChatUtils.initializeChatRemoteService();
        chatRemoteServiceOptional.ifPresent(remoteService -> chatRemoteService = remoteService);

        Timeline chatMessagesRefreshTimeLine = ChatUtils.getChatRefreshTimeline(chatRemoteService, taChat);
        chatMessagesRefreshTimeLine.play();
    }

    public void startNewGame() {
        resetGameUI();
        switch (playerType) {
            case PLAYER_1 -> {
                gameEngine.startNewGame(List.of(
                        new Player(PlayerType.PLAYER_1.toString(), "Player 1"),
                        new Player(PlayerType.PLAYER_2.toString(), "Player 2")));
                if (networkManager != null) {
                    networkManager.sendGameState(gameEngine.getGameState());
                }
                renderGameState();
            }
            case PLAYER_2 -> {
                lbStatus.setText("Waiting for Player 1 to start the game...");
            }
            case SINGLEPLAYER -> {
                gameEngine.startNewGame(List.of(
                        new Player(PlayerType.SINGLEPLAYER.toString(), "Player"),
                        new Player(PlayerType.COMPUTER.toString(), "Computer")
                ));
                renderGameState();
            }
            default -> log.error("Provided player type not valid.");
        }
    }

    private void resetGameUI() {
        hbPlayerHand.setDisable(false);
        hbOpponentHand.setDisable(false);
        spDrawPile.setDisable(false);
        btnUno.setDisable(false);
        btnUno.setVisible(false);

        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();
    }

    public void renderGameState() {
        GameState state = gameEngine.getGameState();
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();

        if (state.isGameOver()) {
            DialogUtils.showWinnerDialog(state.getWinnerName());
            return;
        }

        String currPlayer = state.getCurrentPlayer().getName();
        lbStatus.setText(currPlayer + "'s turn");

        Player me = getLocalPlayer();
        boolean myTurn = gameEngine.isPlayersTurn(me.getId());

        log.info("Current turn: {}", currPlayer);
        log.info("Local player resolved as: {}", me.getName());

        btnUno.setVisible(me.isMustCallUno() && !me.isUnoCalled());
        btnUno.setDisable(!(me.isMustCallUno() && !me.isUnoCalled()));

        for (int i = me.getHand().size() - 1; i >= 0; i--) {
            Card card = me.getHand().get(i);
            Node cardNode = GameUtils.createCardNode(card, true);
            if(config.isEnableAnimations()) {
                AnimationUtils.applyHoverScale(cardNode);
            }

            if (myTurn) {
                cardNode.setDisable(false);
                cardNode.setOpacity(1.0);
                cardNode.setOnMouseClicked(e -> handleCardClick(card));
                cardNode.setCursor(Cursor.HAND);
            } else {
                cardNode.setDisable(true);
                cardNode.setOpacity(0.6);
            }

            hbPlayerHand.getChildren().add(cardNode);
        }

        Player opponent = state.getPlayers().stream()
                .filter(p -> !p.getId().equals(me.getId()))
                .findFirst()
                .orElseThrow();

        int opponentCardCount = opponent.getHand().size();
        for (int i = 0; i < opponentCardCount; i++) {
            hbOpponentHand.getChildren().add(GameUtils.createCardNode(null, false));
        }

        Card topDiscard = state.getDeck().peekTopCard();
        if (topDiscard != null) {
            spDiscardPile.getChildren().add(GameUtils.createCardNode(topDiscard, true));
        }

        Node drawPileNode = GameUtils.createCardNode(null, false);

        if (myTurn) {
            drawPileNode.setOnMouseClicked(e -> handleDrawCardClick());
            drawPileNode.setCursor(Cursor.HAND);
            drawPileNode.setOpacity(1.0);
        } else {
            drawPileNode.setOpacity(0.6);
            drawPileNode.setOnMouseClicked(null);
        }

        spDrawPile.getChildren().add(drawPileNode);

        if (playerType == PlayerType.SINGLEPLAYER && isComputerTurn(state)) {
            runComputerMove();
        }
    }

    private void runComputerMove() {
        GameState state = gameEngine.getGameState();
        Player computer = state.getCurrentPlayer();

        new Thread(() -> {
            try {
                int delay = ThreadLocalRandom.current().nextInt(
                        config.getComputerThinkingDelayMin(),
                        config.getComputerThinkingDelayMax()
                );
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                Card playable = computer.getHand().stream()
                        .filter(card -> gameEngine.isValidMove(card, state.getDeck().peekTopCard()))
                        .findFirst()
                        .orElse(null);

                if (playable != null) {
                    if (playable.getColor() == Color.WILD) {
                        playable.setWildColor(GameUtils.generateRandomColor());
                    }
                    gameEngine.playCard(computer, playable, null);
                } else {
                    gameEngine.drawCard(computer);
                    gameEngine.nextTurn();
                }

                if (computer.isMustCallUno()
                        && !computer.isUnoCalled()
                        && ThreadLocalRandom.current().nextDouble()
                        < config.getComputerCallUnoProbability()) {
                    gameEngine.callUno(computer);
                }

                renderGameState();
            });
        }).start();
    }

    private void handleCardClick(Card card) {
        Player current = getLocalPlayer();
        if (!gameEngine.isPlayersTurn(current.getId())) return;
        if (card.getColor() == Color.WILD) {
            Color chosenColor = DialogUtils.showColorPickerDialog();
            gameEngine.playCard(current, card, chosenColor);
        } else {
            gameEngine.playCard(current, card, null);
        }
        if (networkManager != null) {
            networkManager.sendGameState(gameEngine.getGameState());
        }
        renderGameState();
    }

    private void handleDrawCardClick() {
        Player current = getLocalPlayer();
        lbStatus.setText(current.getName() + "'s turn");
        gameEngine.drawCard(current);
        if (networkManager != null) {
            networkManager.sendGameState(gameEngine.getGameState());
        }
        renderGameState();
    }

    public void saveGame() {
        GameUtils.saveGame(gameEngine.getGameState());
    }

    public void loadGame() {
        GameState loaded = GameUtils.loadGame();
        gameEngine.setGameState(loaded);
        if (networkManager != null) {
            networkManager.sendGameState(gameEngine.getGameState());
        }
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
                    "Error while generating HTML docs.",
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
        Player current = getLocalPlayer();
        if (current.isMustCallUno() && !current.isUnoCalled()) {
            gameEngine.callUno(current);
            btnUno.setVisible(false);
            if (networkManager != null) {
                networkManager.sendGameState(gameEngine.getGameState());
            }
        }
    }

    private boolean isComputerTurn(GameState state) {
        Player current = state.getCurrentPlayer();
        return current.getId().equals(PlayerType.COMPUTER.toString());
    }

    private Player getLocalPlayer() {
        return gameEngine.getPlayerById(playerType.toString());
    }

    public void quitGame() {
        Platform.exit();
    }
}
