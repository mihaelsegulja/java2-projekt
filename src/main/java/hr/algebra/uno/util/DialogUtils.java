package hr.algebra.uno.util;

import hr.algebra.uno.model.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DialogUtils {
    public static void showDialog(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static Color showColorPickerDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Select a color for the Wild card:");

        ButtonType red = new ButtonType("Red");
        ButtonType yellow = new ButtonType("Yellow");
        ButtonType green = new ButtonType("Green");
        ButtonType blue = new ButtonType("Blue");

        dialog.getButtonTypes().setAll(red, yellow, green, blue);

        //TODO: move to a css file
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

        if (result == red)    return Color.Red;
        if (result == yellow) return Color.Yellow;
        if (result == green)  return Color.Green;
        if (result == blue)   return Color.Blue;

        return null;
    }
}
