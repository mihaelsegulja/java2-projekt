package hr.algebra.uno.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameState implements Serializable {
    private List<Player> players =  new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean clockwise = true;
    private Deck deck = new Deck();
    private boolean gameOver = false;
    private String winnerName;

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
}
