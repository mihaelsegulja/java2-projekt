package hr.algebra.uno.network;

import hr.algebra.uno.controller.GameController;
import hr.algebra.uno.engine.GameEngine;
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
    private final String host = "localhost";
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
            System.out.printf("[%s] Listening on port %d%n", playerType, listenPort);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("[%s] Client connected from %s%n", playerType, socket.getInetAddress());
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
            System.out.println("[" + playerType + "] Received GameState from remote player.");
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
        try (Socket socket = new Socket(host, targetPort)) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(state);
            System.out.printf("[%s] GameState sent to port %d%n", playerType, targetPort);
            System.out.println("Response: " + ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }
}
