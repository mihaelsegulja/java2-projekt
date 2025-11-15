package hr.algebra.uno.model;

import lombok.Getter;

public enum PlayerType {
    Singleplayer(100),
    Player_1(0),
    Player_2(1);

    @Getter
    private int index;

    PlayerType(int index) {
        this.index = index;
    }
}
