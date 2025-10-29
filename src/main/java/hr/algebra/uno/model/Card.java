package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Card {
    private final Color color;
    private final Value value;
}
