package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameState implements java.io.Serializable {
    private List<Player> players =  new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean clockwise = true;
    private Deck deck = new Deck();
    private boolean gameOver = false;

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}
