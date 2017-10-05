import java.io.Serializable;
import java.util.Vector;

public class GameState implements Serializable {
	Vector<Player> players;
	Vector<Treasure> treasures;
	
	public GameState(Vector<Player> players, Vector<Treasure> treasures) {
		this.players = (Vector<Player>) players.clone();
		this.treasures = (Vector<Treasure>) treasures.clone();
	}
}