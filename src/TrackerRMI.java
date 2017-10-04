import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TrackerRMI extends Remote {	
	AddPlayerReply addPlayer(String myHost, int myPort, String myName) throws RemoteException;
	void removePlayer(String myName, String serverName) throws RemoteException;
	void removeAllPreviousPlayers(String myName, String serverName) throws RemoteException;
}