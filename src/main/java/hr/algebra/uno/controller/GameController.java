package hr.algebra.uno.controller;

import hr.algebra.uno.util.DocumentationUtils;
import javafx.event.ActionEvent;

import java.io.IOException;

public class GameController {

    public void startNewGame() {

    }

    public void saveGame() {

    }

    public void loadGame() {

    }

    public void generateDocumentation(ActionEvent actionEvent) {
        try {
            DocumentationUtils.generateDocumentationHtmlFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
