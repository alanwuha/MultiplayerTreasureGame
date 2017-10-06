import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class Tracker implements TrackerRMI {
	private Vector<PlayerInfo> players;
	private int port;
	private int N;
	private int K;
	private int cellSize;
	private Registry registry;
	
	public Tracker(int port, int N, int K, int cellSize) {
		this.port = port;
		this.players = new Vector<PlayerInfo>();
		this.N = N;
		this.K = K;
		this.cellSize = cellSize;
		
		try {
			System.out.println("Tracker's IP Host Address: " + InetAddress.getLocalHost().getHostAddress());
			registry = LocateRegistry.getRegistry(port);
		} catch (Exception e) {
			System.out.println("Error");
		}
	}
		
	public AddPlayerReply addPlayer(String myHost, int myPort, String myName, GameRMI g) {	
		// Bind player to registry
		try {
			registry.rebind(myName, g);
		} catch (Exception e) {
			System.err.println("Bind error :(");
			e.printStackTrace();
		}
		
		// Add player (Check if host and port combination is unique? Probably not...)
		players.add(new PlayerInfo(myHost, myPort, myName));
		
		System.out.println("Player " + myName + " (" + myHost + ") has joined the game! Number of players online: " + players.size());
		
		// Reply client
		return new AddPlayerReply(players, N, K, cellSize);
	}
	
	public void removePlayer(String myName, String serverName) {
		System.out.println("Removing Player " + myName + " by Player " + serverName);
		
		for(PlayerInfo p : players) {
			if(p.name.equals(myName)) {
				players.remove(p);
				break;
			}
		}
		
		System.out.println("Player " + myName + " has left! Number of players online: " + players.size());
		
		printPlayers();
	}
	
	public void removeAllPreviousPlayers(String myName) {		
		for(PlayerInfo p : players) {
			if(!p.name.equals(myName)) {
				System.out.println("Removing Player " + p.name + " by Player " + myName);
				players.remove(p);
			} else {
				break;
			}
		}
		
		printPlayers();
	}
	
	public void printPlayers() {
		String output = "";
		for(PlayerInfo p : players) {
			output += p.name + " ";
		}
		
		System.out.println(output);
	}
	
	public static void main(String args[]) {
		// Check for valid number of command line arguments
		if(args.length != 3) {
			System.out.println("Wrong number of parameters...exiting");
			System.exit(0);
		}
		
		int port = Integer.parseInt(args[0]);
		int N = Integer.parseInt(args[1]);
		int K = Integer.parseInt(args[2]);	
		
		TrackerRMI stub = null;
		Registry registry = null;

		try {
			Tracker t = new Tracker(port, N, K, 30);
			stub = (TrackerRMI) UnicastRemoteObject.exportObject(t, 0);
			registry = LocateRegistry.getRegistry(port);
			registry.bind("TrackerRMI", stub);
			
			System.out.println("Tracker Ready");
		} catch (Exception e1) {
			try {
				registry.unbind("TrackerRMI");
				registry.bind("TrackerRMI", stub);
				System.out.println("Tracker Ready");
			} catch (Exception e2) {
				System.out.println("Tracker exception: " + e2.toString());
				e2.printStackTrace();
			}
		}
	}
}