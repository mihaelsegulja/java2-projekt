package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Card implements java.io.Serializable {
    private Color color;
    private final Value value;

    public void setWildColor(Color chosenColor) {
        if ((this.value == Value.Wild || this.value == Value.Wild_Draw_Four) && this.color == Color.Wild) {
            this.color = chosenColor;
        }
    }
}
