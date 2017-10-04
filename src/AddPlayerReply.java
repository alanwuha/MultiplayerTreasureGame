import java.io.Serializable;
import java.util.Vector;

public class AddPlayerReply implements Serializable {
	public Vector<PlayerInfo> players;
	public int N;
	public int K;
	public int cellSize;
	
	public AddPlayerReply(Vector<PlayerInfo> players, int N, int K, int cellSize) {
		this.players = (Vector<PlayerInfo>) players.clone();
		this.N = N;
		this.K = K;
		this.cellSize = cellSize;
	}
}