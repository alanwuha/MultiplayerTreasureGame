import java.io.Serializable;
import java.util.Vector;

public class MakeMoveReply implements Serializable {
	Vector<Player> players;
	Vector<Treasure> treasures;
	
	public MakeMoveReply(Vector<Player> players, Vector<Treasure> treasures) {
		this.players = (Vector<Player>) players.clone();
		this.treasures = (Vector<Treasure>) treasures.clone();
	}
}