package hr.algebra.uno.network;

import hr.algebra.uno.controller.GameController;
import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.jndi.ConfigurationKey;
import hr.algebra.uno.jndi.ConfigurationReader;
import hr.algebra.uno.model.GameState;
import hr.algebra.uno.model.PlayerType;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
    private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
    private final PlayerType playerType;
    private final int listenPort;
    private final int targetPort;
    private static final String HOST = ConfigurationReader.getStringValueForKey(ConfigurationKey.HOSTNAME);
    private GameController gameController;

    public NetworkManager(PlayerType playerType, int listenPort, int targetPort, GameController gameController) {
        this.playerType = playerType;
        this.listenPort = listenPort;
        this.targetPort = targetPort;
        this.gameController = gameController;
    }

    public void startServer(GameEngine engine) {
        new Thread(() -> acceptConnections(engine)).start();
    }

    private void acceptConnections(GameEngine engine) {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            log.info("[{}] Listening on port {}", playerType, listenPort);

            while (true) {
                Socket socket = serverSocket.accept();
                log.info("[{}] Client connected from {}", playerType, socket.getInetAddress());
                new Thread(() -> handleClient(socket, engine)).start();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handleClient(Socket socket, GameEngine engine) {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            GameState received = (GameState) ois.readObject();
            log.info("[{}] Received GameState from remote player.", playerType);
            engine.setGameState(received);

            Platform.runLater(() -> {
                gameController.renderGameState();
            });
            oos.writeObject("ACK");

        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendGameState(GameState state) {
        try (Socket socket = new Socket(HOST, targetPort)) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(state);
            log.info("{}, {}", state.getCurrentPlayer().getId(), state.getDeck().peekTopCard());
            log.info("[{}] GameState sent to port {}", playerType, targetPort);
            log.info("Response: {}", ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }
}
