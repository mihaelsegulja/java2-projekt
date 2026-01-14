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
    private ChatRemoteService chatRemoteService;
    private boolean computerThinking = false;


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
                syncNetworkState();
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
        clearBoard();
    }

    public void renderGameState() {
        GameState state = gameEngine.getGameState();
        clearBoard();

        if (handleGameOver(state)) return;

        updateTurnStatus(state);
        updateUnoButton();
        renderPlayerHand(state);
        renderOpponentHand(state);
        renderDiscardPile(state);
        renderDrawPile(state);
        maybeRunComputerMove(state);
    }

    private static boolean handleGameOver(GameState state) {
        if (!state.isGameOver()) return false;
        DialogUtils.showWinnerDialog(state.getWinnerName());
        return true;
    }

    private void updateTurnStatus(GameState state) {
        lbStatus.setText(state.getCurrentPlayer().getName() + "'s turn");
    }

    private void updateUnoButton() {
        Player me = getLocalPlayer();
        boolean mustCall = me.isMustCallUno() && !me.isUnoCalled();
        btnUno.setVisible(mustCall);
        btnUno.setDisable(!mustCall);
    }

    private void renderPlayerHand(GameState state) {
        Player me = getLocalPlayer();
        boolean myTurn = gameEngine.isPlayersTurn(me.getId());

        for (int i = me.getHand().size() - 1; i >= 0; i--) {
            Card card = me.getHand().get(i);
            Node node = createPlayerCardNode(card, state, myTurn);
            hbPlayerHand.getChildren().add(node);
        }
    }

    private Node createPlayerCardNode(Card card, GameState state, boolean myTurn) {
        Node node = UIUtils.createCardNode(card, true);

        if (config.isEnableAnimations()) AnimationUtils.applyHoverScale(node);

        if (isPlayableHint(card, state, myTurn)) {
            node.setStyle("""
            -fx-effect: dropshadow(gaussian, rgba(255, 0, 132, 0.9), 15, 0.5, 0, 0);
        """);
        }

        configureCardInteraction(node, card, myTurn);
        return node;
    }

    private boolean isPlayableHint(Card card, GameState state, boolean myTurn) {
        return myTurn
                && config.isShowPlayableHints()
                && gameEngine.isValidMove(card, state.getDeck().peekTopCard());
    }

    private void configureCardInteraction(Node node, Card card, boolean myTurn) {
        if (!myTurn) {
            node.setDisable(true);
            node.setOpacity(0.6);
            return;
        }

        node.setDisable(false);
        node.setOpacity(1.0);
        node.setCursor(Cursor.HAND);
        node.setOnMouseClicked(e -> handleCardClick(card));
    }

    private void renderOpponentHand(GameState state) {
        Player opponent = state.getPlayers().stream()
                .filter(p -> !p.getId().equals(getLocalPlayer().getId()))
                .findFirst()
                .orElseThrow();

        for (int i = 0; i < opponent.getHand().size(); i++) {
            hbOpponentHand.getChildren().add(UIUtils.createCardNode(null, false));
        }
    }

    private void renderDiscardPile(GameState state) {
        Card top = state.getDeck().peekTopCard();
        if (top != null) {
            spDiscardPile.getChildren().add(UIUtils.createCardNode(top, true));
        }
    }

    private void renderDrawPile(GameState state) {
        Node drawNode = UIUtils.createCardNode(null, false);
        boolean myTurn = gameEngine.isPlayersTurn(getLocalPlayer().getId());

        drawNode.setOpacity(myTurn ? 1.0 : 0.6);

        if (myTurn) {
            drawNode.setCursor(Cursor.HAND);
            drawNode.setOnMouseClicked(e -> handleDrawCardClick());
        }

        spDrawPile.getChildren().add(drawNode);
    }

    private void maybeRunComputerMove(GameState state) {
        if (playerType == PlayerType.SINGLEPLAYER && isComputerTurn(state)) {
            runComputerMove();
        }
    }

    private void runComputerMove() {
        if (computerThinking) return;
        computerThinking = true;
        ComputerHelper.performMove(gameEngine, config, () -> {
            computerThinking = false;
            syncNetworkState();
            renderGameState();
        });
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
        syncNetworkState();
        renderGameState();
    }

    private void handleDrawCardClick() {
        Player current = getLocalPlayer();
        lbStatus.setText(current.getName() + "'s turn");
        gameEngine.drawCard(current);
        syncNetworkState();
        renderGameState();
    }

    public void saveGame() {
        GameUtils.saveGame(gameEngine.getGameState());
    }

    public void loadGame() {
        GameState loaded = GameUtils.loadGame();
        gameEngine.setGameState(loaded);
        syncNetworkState();
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
            syncNetworkState();
        }
    }

    private boolean isComputerTurn(GameState state) {
        Player current = state.getCurrentPlayer();
        return current.getId().equals(PlayerType.COMPUTER.toString());
    }

    private Player getLocalPlayer() {
        return gameEngine.getPlayerById(playerType.toString());
    }

    private void syncNetworkState() {
        if (networkManager != null) {
            networkManager.sendGameState(gameEngine.getGameState());
        }
    }

    private void clearBoard() {
        hbPlayerHand.getChildren().clear();
        hbOpponentHand.getChildren().clear();
        spDrawPile.getChildren().clear();
        spDiscardPile.getChildren().clear();
    }

    public void quitGame() {
        Platform.exit();
    }
}
