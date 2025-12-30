package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public class Card implements Serializable {
    private Color color;
    private final Value value;

    public void setWildColor(Color chosenColor) {
        if ((this.value == Value.WILD || this.value == Value.WILD_DRAW_FOUR) && this.color == Color.WILD) {
            this.color = chosenColor;
        }
    }
}
