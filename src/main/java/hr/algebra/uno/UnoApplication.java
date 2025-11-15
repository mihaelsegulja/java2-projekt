package hr.algebra.uno;

import hr.algebra.uno.model.PlayerType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class UnoApplication extends Application {
    public static PlayerType playerType;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You must supply a player type.");
            JOptionPane.showMessageDialog(null, "You must supply a player type.");
            System.exit(1);
        }

        String firstArg = args[0];

        boolean valid = false;
        for (PlayerType pt : PlayerType.values()) {
            if (pt.name().equals(firstArg)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            System.out.println("Invalid player type: " + firstArg);
            JOptionPane.showMessageDialog(null, "You provided a player type that does not exist!");
            System.exit(1);
        }

        UnoApplication.playerType = PlayerType.valueOf(firstArg);
        Application.launch(UnoApplication.class, args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UnoApplication.class.getResource("game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Uno - " + playerType);
        stage.setScene(scene);
        stage.show();
    }
}
