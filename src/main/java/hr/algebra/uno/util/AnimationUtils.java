package hr.algebra.uno.util;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class AnimationUtils {

    private AnimationUtils() {}

    public static void applyHoverScale(Node node) {
        ScaleTransition enter = new ScaleTransition(Duration.millis(120), node);
        enter.setToX(1.1);
        enter.setToY(1.1);

        ScaleTransition exit = new ScaleTransition(Duration.millis(120), node);
        exit.setToX(1.0);
        exit.setToY(1.0);

        node.setOnMouseEntered(e -> enter.playFromStart());
        node.setOnMouseExited(e -> exit.playFromStart());
    }
}

