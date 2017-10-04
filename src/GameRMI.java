import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface GameRMI extends Remote {
	// Methods invoked by Player to Primary Server
	void joinGame(String playerName, String playerHost, int playerPort) throws RemoteException;
	void makeMove(char keyString, String playerName) throws RemoteException;
	void leaveGame(String name) throws RemoteException;
	
	// Methods invoked by Server to Player
	void assignAsServer(Vector<Player> players, Vector<Treasure> treasures) throws RemoteException;
	void spawnPingThread() throws RemoteException;
	//void ping() throws RemoteException;
}