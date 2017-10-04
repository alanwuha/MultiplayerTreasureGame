import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game {
	// Game variables
	public static int cellSize;
	public static int N;
	public static int K;
	public static int width;
	
	// Graphics
	private JPanel panel;
	private JFrame frame;
	
	// Players and treasures
	private Vector<PlayerInfo> playersInfo = new Vector<PlayerInfo>();
	public static Vector<Player> players = new Vector<Player>();
	public static Vector<Treasure> treasures = new Vector<Treasure>();
	
	// RMI variables
	private Registry registry;
	private String trackerHost;
	private int trackerPort;
	private TrackerRMI trackerRMIRef;
	private String playerName;
	private String playerHost;
	private int playerPort;
	
	public Game(String playerName, String trackerHost, int trackerPort) {
		this.playerName = playerName;
		this.trackerHost = trackerHost;
		this.trackerPort = trackerPort;
	};
	
	public boolean init() {
		try {
			// Get player host and port
			playerHost = InetAddress.getLocalHost().getHostAddress();
			playerPort = trackerPort;
			
			// Locate registry
			this.registry = LocateRegistry.getRegistry(trackerHost, trackerPort);
			
			// Lookup TrackerRMI remote object to get players list and game specs
			trackerRMIRef = (TrackerRMI) registry.lookup("TrackerRMI");
			AddPlayerReply reply = trackerRMIRef.addPlayer(playerHost, playerPort, playerName);
			this.playersInfo = (Vector<PlayerInfo>) reply.players.clone();
			Game.N = reply.N;
			Game.K = reply.K;
			Game.cellSize = reply.cellSize;
			Game.width = N * cellSize;
			
			// Generate or get players/treasures
			if(playersInfo.size() == 1) {
				// First player's position can be anywhere on the grid
				Random random = new Random();
				players.add(new Player(playerHost, playerPort, playerName, random.nextInt(Game.N - 1), random.nextInt(Game.N - 1), 0, getRandomColor()));
				
				// Generate treasures
				for(int i = 0; i < Game.K; ++i) {
					Treasure t = new Treasure();
					Position pos = pickNewPosition();
					t.x = pos.x;
					t.y = pos.y;
					treasures.add(t);
				}
			} else {
				// Contact server to get game state
				// ...
			}
			
			// Create JPanel
			panel = new Screen();
			
			// Create JFrame
			frame = new JFrame();
			frame.setTitle(playerName);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			frame.setFocusable(true);
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			// Initiate game loop
			// ...
			
			// Spawn thread to perform pinging if player is primary
			// ...
		} catch (Exception e) {
			System.err.println("Game: init() error.");
			e.printStackTrace();
		}
		
		return true;
	}
	
	private Color getRandomColor() {
 		Random rand = new Random();
 		return new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
 	}
	
	private Position pickNewPosition() {
		Random random = new Random();
		int randX = 0;
		int randY = 0;
		
		boolean isUniqueLocation = false;
		
		while(!isUniqueLocation) {
			randX = random.nextInt(Game.N - 1);
			randY = random.nextInt(Game.N - 1);
			
			isUniqueLocation = true;
			
			// Check against treasures
			for(Treasure t : treasures) {
				if(randX == t.x && randY == t.y) {
					isUniqueLocation = false;
					break;
				}
			}
			
			if(isUniqueLocation) {
				// Check against players				
				for(Player p : players) {
					if(randX == p.x && randY == p.y) {
						isUniqueLocation = false;
						break;
					}
				}
				
			}
		}
		
		return new Position(randX, randY);
	}
	
	public static void main(String args[]) {
		System.out.println("Welcome to the game!");
		
		// Check for valid number of command line arguments
		if(args.length != 3) {
			System.out.print("Wrong number of parameters...exiting");
			System.exit(0);
		}
		
		// Get command line values
		String trackerHost = args[0];
		int trackerPort = Integer.parseInt(args[1]);
		String playerName = args[2];
		
		// Create game
		Game g = new Game(playerName, trackerHost, trackerPort);
		if(!g.init()) {
			System.err.println("Main: Failed to initialize Game.");
			System.exit(-1);
		}
		
		// Get user input
		Scanner s = new Scanner(System.in);
		char c = s.next().charAt(0);
		while(c != '9') {
			if(c == '0' || c == '2' || c == '3' || c == '4') {
				// ...
			}
			
			// Get next user input
			c = s.next().charAt(0);
		}
		
		// Close scanner
		s.close();
		
		// Player is quitting...
		// g.stopGameLoop();
		
		// Notify that you're quitting
		// g.quit(); Include stopGameLoop here?
		
		// Exit program
		System.out.println("Exiting program...");
		System.exit(0);
	}	
}
