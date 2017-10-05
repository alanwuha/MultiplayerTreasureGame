import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface GameRMI extends Remote {
	// Methods invoked by Player to Primary Server
	GameState joinGame(String playerName, String playerHost, int playerPort) throws RemoteException;
	GameState makeMove(char keyString, String playerName) throws RemoteException;
	void leaveGame(String name) throws RemoteException;
	
	// Methods invoked by Server to Player
	void updateGameState(Vector<Player> players, Vector<Treasure> treasures) throws RemoteException;
	void assumeRoleAsServer(Vector<Player> players, Vector<Treasure> treasures) throws RemoteException;
	void spawnPingThread() throws RemoteException;
	void ping() throws RemoteException;
}