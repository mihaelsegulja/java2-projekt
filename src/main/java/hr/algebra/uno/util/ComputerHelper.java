package hr.algebra.uno.util;

import hr.algebra.uno.config.GameConfig;
import hr.algebra.uno.engine.GameEngine;
import hr.algebra.uno.model.*;
import javafx.application.Platform;

import java.util.concurrent.ThreadLocalRandom;

public final class ComputerHelper {

    private ComputerHelper() {}

    public static void performMove(GameEngine engine, GameConfig config, Runnable afterMove) {
        new Thread(() -> {
            sleepRandom(config);

            Platform.runLater(() -> {
                play(engine, config);
                afterMove.run();
            });
        }).start();
    }

    private static void sleepRandom(GameConfig config) {
        try {
            int delay = ThreadLocalRandom.current().nextInt(
                    config.getComputerThinkingDelayMin(),
                    config.getComputerThinkingDelayMax()
            );
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
    }

    private static void play(GameEngine engine, GameConfig config) {
        GameState state = engine.getGameState();
        Player computer = state.getCurrentPlayer();

        Card playable = computer.getHand().stream()
                .filter(card -> engine.isValidMove(card, state.getDeck().peekTopCard()))
                .findFirst()
                .orElse(null);

        if (playable != null) {
            Color chosenColor = requiresColor(playable)
                    ? GameUtils.generateRandomColor()
                    : null;

            engine.playCard(computer, playable, chosenColor);
        } else {
            engine.drawCard(computer);
            engine.nextTurn();
        }

        maybeCallUno(computer, config, engine);
    }

    private static boolean requiresColor(Card card) {
        return card.getValue() == Value.WILD || card.getValue() == Value.WILD_DRAW_FOUR;
    }

    private static void maybeCallUno(Player computer, GameConfig config, GameEngine engine) {
        if (computer.isMustCallUno()
                && !computer.isUnoCalled()
                && ThreadLocalRandom.current().nextDouble()
                < config.getComputerCallUnoProbability()) {
            engine.callUno(computer);
        }
    }
}