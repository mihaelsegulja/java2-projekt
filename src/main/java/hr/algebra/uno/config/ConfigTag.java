package hr.algebra.uno.config;

public enum ConfigTag {

    GAME_CONFIG("game-config"),

    RULES("rules"),
    STARTING_CARDS("starting-cards"),
    MUST_CALL_UNO("must-call-uno"),
    UNO_PENALTY_CARDS("uno-penalty-cards"),
    ALLOW_STACKING("allow-stacking"),
    DRAW_UNTIL_PLAYABLE("draw-until-playable"),
    INITIAL_DIRECTION("initial-direction"),

    COMPUTER("computer"),
    THINKING_DELAY_MIN("thinking-delay-min"),
    THINKING_DELAY_MAX("thinking-delay-max"),
    CALL_UNO_PROBABILITY("call-uno-probability"),

    UI("ui"),
    ENABLE_ANIMATIONS("enable-animations"),
    SHOW_PLAYABLE_HINTS("show-playable-hints");

    private final String tag;

    ConfigTag(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }
}