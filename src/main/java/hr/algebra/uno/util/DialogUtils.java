package hr.algebra.uno.util;

import hr.algebra.uno.model.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DialogUtils {
    private DialogUtils() {}

    public static void showDialog(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showWinnerDialog(String winnerName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("We have a winner!");
        alert.setContentText(winnerName + " has won the game.");
        alert.show();
    }

    public static Color showColorPickerDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Select a color for the WILD card:");

        ButtonType red = new ButtonType("RED");
        ButtonType yellow = new ButtonType("YELLOW");
        ButtonType green = new ButtonType("GREEN");
        ButtonType blue = new ButtonType("BLUE");

        dialog.getButtonTypes().setAll(red, yellow, green, blue);

        dialog.getDialogPane().applyCss();

        dialog.getDialogPane().lookupButton(red).setStyle(
                "-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;"
        );

        dialog.getDialogPane().lookupButton(yellow).setStyle(
                "-fx-background-color: yellow; -fx-text-fill: black; -fx-font-size: 16px; -fx-padding: 10px;"
        );

        dialog.getDialogPane().lookupButton(green).setStyle(
                "-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;"
        );

        dialog.getDialogPane().lookupButton(blue).setStyle(
                "-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;"
        );

        ButtonType result = dialog.showAndWait().orElse(null);

        if (result == red)    return Color.RED;
        if (result == yellow) return Color.YELLOW;
        if (result == green)  return Color.GREEN;
        if (result == blue)   return Color.BLUE;

        return null;
    }
}
