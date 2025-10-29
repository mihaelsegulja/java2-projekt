package hr.algebra.uno.controller;

import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.model.GameState;
import hr.algebra.uno.util.DialogUtils;
import hr.algebra.uno.util.DocumentationUtils;
import hr.algebra.uno.util.GameUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.io.IOException;

public class GameController {
    private GameEngine gameEngine = new GameEngine();

    public void startNewGame() {
        gameEngine.startNewGame();
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
        } catch (IOException e) {
            DialogUtils.showDialog("Error",
                    "Something went wrong while generating HTML docs.",
                    Alert.AlertType.ERROR);
        }
    }
}
