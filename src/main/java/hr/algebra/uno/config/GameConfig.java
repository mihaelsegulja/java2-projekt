package hr.algebra.uno.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameConfig {
    private int startingCards = 7;
    private boolean mustCallUno = true;
    private int unoPenaltyCards = 2;
    private boolean allowStacking = false;
    private boolean drawUntilPlayable = false;
    private Direction initialDirection = Direction.CLOCKWISE;

    private int computerThinkingDelayMin = 500;
    private int computerThinkingDelayMax = 1500;
    private double computerCallUnoProbability = 0.9;

    private boolean enableAnimations = true;
    private boolean showPlayableHints = false;
}
