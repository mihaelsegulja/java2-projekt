package hr.algebra.uno.model;

import lombok.Getter;

public enum PlayerType {
    PLAYER_1(0),
    PLAYER_2(1),
    SINGLEPLAYER(100),
    COMPUTER(101);

    @Getter
    private int index;

    PlayerType(int index) {
        this.index = index;
    }
}
