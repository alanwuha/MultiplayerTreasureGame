import java.awt.Color;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game implements GameRMI {
	// Game variables
	public final static int lowestPortNum = 2002;
	public static int playerID = 0;
	public static int cellSize;
	public static int N;
	public static int K;
	public static int width;
	
	// Graphics
	private JPanel panel;
	private JFrame frame;
	private boolean isRunning;
	
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
		this.isRunning = true;
		this.playerID++;
	};
	
	public boolean init() {
		try {
			// Get player host and port
			playerHost = InetAddress.getLocalHost().getHostAddress();
			playerPort = (playerID % 10) + lowestPortNum;
			
			// Locate registry
			registry = LocateRegistry.getRegistry(trackerHost, trackerPort);
			
			// Bind self to registry
			GameRMI gRMI = (GameRMI) UnicastRemoteObject.exportObject(this, 0);
			
			// Lookup TrackerRMI remote object to get players list and game specs
			trackerRMIRef = (TrackerRMI) registry.lookup("TrackerRMI");
			AddPlayerReply reply = trackerRMIRef.addPlayer(InetAddress.getLocalHost().getHostAddress(), trackerPort, playerName, gRMI);
			
			playersInfo = (Vector<PlayerInfo>) reply.players.clone();
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
				boolean isConnected = false;
				int i = 0;
				// If server is uncontactable, iterate through other players to find the server
				// playersInfo.size() - 1 so that player does not contact itself to joinGame
				// but rather just the subsequent code to create a new game
				while(!isConnected && i < playersInfo.size() - 1) {
					try {					
						System.out.println("Contacting Player \"" + playersInfo.get(i).name + "\" to join game...");
						
						// Lookup remote object to join game
						GameRMI g = (GameRMI) registry.lookup(playersInfo.get(i).name);
						GameState state = g.joinGame(playerName, playerHost, playerPort);
						
						// Update game state
						players = (Vector<Player>) state.players.clone();
						treasures = (Vector<Treasure>) state.treasures.clone();
						
						isConnected = true;
						
						System.out.println("Successfully contact Player \"" + playersInfo.get(i).name + "\" to join game.");
					} catch (Exception ee) {
						System.out.println("Join game error: Failed to contact Player " + playersInfo.get(i).name + " to join game.");
					}
					
					++i;
				}
					
				if(!isConnected) {
					try {
						// Reset Tracker
						trackerRMIRef.removeAllPreviousPlayers(this.playerName);
						
						// First player's position can be anywhere on the grid
						Random random = new Random();
						players.add(new Player(playerHost, playerPort, playerName, random.nextInt(Game.N - 1), random.nextInt(Game.N - 1), 0, getRandomColor()));
						
						// Generate treasures
						for(int j = 0; j < Game.K; ++j) {
							Treasure t = new Treasure();
							Position pos = pickNewPosition();
							t.x = pos.x;
							t.y = pos.y;
							treasures.add(t);
						}
						//screen.init(players, treasures);
					} catch (Exception e) {
						System.out.println("Game init error: Player " + this.playerName + " failed to removeAllPlayers on Tracker.");
					}				
				}
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
			
			// SpawnPingThread
			if(players.size() == 1) spawnPingThread();
			
		} catch (Exception e) {
			System.err.println("Game: init() error.");
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public GameState joinGame(String playerName, String playerHost, int playerPort) {
		Player p = new Player(playerHost, playerPort, playerName, 0, 0, 0, getRandomColor());
		
		// Assign player a location
		Position pos = pickNewPosition();
		p.x = pos.x;
		p.y = pos.y;
		
		// Add player to list
		players.add(p);
		
		System.out.println("Player " + p.name + " has joined the game!");
		
		// If not primary server, assume role as server
		if(!players.firstElement().name.equals(this.playerName)) {
			while(!players.firstElement().name.equals(this.playerName)) {
				leaveGame(players.firstElement().name);
			}
			
			try {
				// Also, remove old players from Tracker that might not have been removed through leaveGame()
				trackerRMIRef.removeAllPreviousPlayers(this.playerName);
			} catch (Exception e) {
				System.err.println("Game makeMove failed: Failed to removeAllPreviousPlayers from Tracker.");
			}
			
			// Spawn ping thread
			spawnPingThread();
		}
		
		// Repaint server's panel
		panel.repaint();
		
		// Update backup server
		updateBackupServer();
		
		return new GameState(players, treasures);
	}
	
	@Override
	public GameState makeMove(char keyString, String playerName) {
		// Get reference to player
		Player player = new Player();
		boolean playerFound = false;
		for(Player p : players) {
			if(p.name.equals(playerName)) {
				// Get reference of requesting player
				player = p;
				playerFound = true;
				break;
			}
		}
		
		// If player doesn't exist, return
		if(!playerFound) {
			return null;
		}
		
		// Move player
		movePlayer(keyString, player);
		
		// Check if player has found a treasure
		// This method implicitly assigns a new position for the collected treasure
		foundTreasure(player);
		
		// If not primary server, assume role as server
		if(!players.firstElement().name.equals(this.playerName)) {
			while(!players.firstElement().name.equals(this.playerName)) {
				leaveGame(players.firstElement().name);
			}
			
			try {
				// Also, remove old players from Tracker that might not have been removed through leaveGame()
				trackerRMIRef.removeAllPreviousPlayers(this.playerName);
			} catch (Exception e) {
				System.err.println("Game makeMove failed: Failed to removeAllPreviousPlayers from Tracker.");
			}
			
			// Spawn ping thread
			spawnPingThread();
		}
		
		// Repaint panel
		panel.repaint();
		
		// Update backup server
		updateBackupServer();
		
		return new GameState(players, treasures);
	}
	
	@Override
	public void leaveGame(String playerName) {
		try {
			// Remove player from primary server's players list
			for(Player p : players) {
				if(p.name.equals(playerName)) {
					// If player exists, remove player
					players.remove(p);
					
					// Repaint panel
					panel.repaint();
					
					// Update backup server
					updateBackupServer();
					
					// Update tracker
					trackerRMIRef.removePlayer(playerName, this.playerName);
					
					System.out.println("Player " + playerName + " has left the game!");
					
					return;
				}
			}
		} catch (Exception e) {
			System.out.println("leaveGame failed: Player " + this.playerName + "failed to contact Tracker to remove Player " + playerName);
		}
	}
	
	@Override
	public void assumeRoleAsServer(Vector<Player> players, Vector<Treasure> treasures) {
		this.players = (Vector<Player>) players.clone();
		this.treasures = (Vector<Treasure>) treasures.clone();
		
		// Repaint panel
		panel.repaint();
		
		// Spawn ping thread
		spawnPingThread();
	}
	
	@Override
	public void spawnPingThread() {
		System.out.println("spawnPingThread");
		
		// Spawn thread to run pings
		Thread loop = new Thread() {
			public void run() {
				try {
					pingAllPlayers();
				} catch (Exception e) {
					System.out.println("spawnPingThread error: " + e.getMessage());
				}
			}
		};
		
		// Start thread
		loop.start();
	}
	
	@Override
	public void ping() {}
	
	@Override
	public void updateGameState(Vector<Player> players, Vector<Treasure> treasures) {
		this.players = (Vector<Player>) players.clone();
		this.treasures = (Vector<Treasure>) treasures.clone();
		
		// Repaint panel
		panel.repaint();
	}
	
	public void assignNewServer() {
		if(!players.isEmpty()) {
			try {
				// Update next player's game state
				GameRMI nextPlayerRMI = (GameRMI) registry.lookup(players.firstElement().name);
				nextPlayerRMI.assumeRoleAsServer(players, treasures);
			} catch (Exception e) {
				System.out.println("assignNewServer failed: Player " + this.playerName + "failed to contact backup Player " + playerName);
			}
		}
	}
	
	public void contactServerToMove(char c) {
		for(Player p : players) {
			try {
				GameRMI serverGameRMIRef = (GameRMI) registry.lookup(p.name);
				GameState reply = serverGameRMIRef.makeMove(c, playerName);
				
				// Update game state
				players = (Vector<Player>) reply.players.clone();
				treasures = (Vector<Treasure>) reply.treasures.clone();
				
				return;
			} catch (Exception e) {
				System.out.println("contactServerToMove failed: Primary Player " + p.name + " could not be contacted. Contacting next player...");
				
				try {
					Thread.sleep(10);
				} catch (Exception e2) {
					
				}
			}
		}
	}
	
	public void requestToLeaveGame() {
		try {
			GameRMI serverGameRMIRef = (GameRMI) registry.lookup(players.firstElement().name);
			serverGameRMIRef.leaveGame(playerName);
		} catch (Exception e) {
			System.err.println("Game requestToLeaveGame failed: Failed to contact server.");
		}
	}
	
	public void unbind() {
		try {
			registry.unbind(playerName);
		} catch (Exception e) {
			System.err.println("Game unbind() failed.");
		}
	}
	
	private void pingAllPlayers() throws InterruptedException {
		while(isRunning) {
			// Ping all players
			for(int i = 0; i < players.size(); ++i) {
				if(players.get(i).name.equals(this.playerName)) continue;
				
				try {
					GameRMI gRMI = (GameRMI) registry.lookup(players.get(i).name);
					gRMI.ping();
				} catch (Exception e) {
					try {
						// Ping failed meaning player has crashed
						// Remove player from list
						System.out.println("Ping to Player " + players.get(i).name + " failed. Removing Player " + players.get(i).name);
						leaveGame(players.get(i).name);	// Leave game implicitly removes player from Tracker
					}
					catch (Exception e2) {
						System.out.println("pingAllPlayers: Array index out of bounds or leaveGame() error.");
					}
					
					updateBackupServer();
				}
			}
			
			// Sleep for 0.5 seconds after each iteration of pings
			Thread.sleep(500);
		}
	}
	
	private void movePlayer(char keyString, Player p) {
		// Check if move is valid (never hit wall and no collision)
		switch(keyString) {
		case '1':
			if(p.x > 0 && !hasCollisionWithPlayer(p.x-1, p.y)) {
				p.x--;
			}
			break;
		case '2':
			if(p.y < Game.N - 1 && !hasCollisionWithPlayer(p.x, p.y+1)) {
				p.y++;
			}
			break;
		case '3':
			if(p.x < Game.N - 1 && !hasCollisionWithPlayer(p.x+1, p.y)) {
				p.x++;
			}
			break;
		case '4':
			if(p.y > 0 && !hasCollisionWithPlayer(p.x, p.y-1)) {
				p.y--;
			}
			break;
		}
		
		// System.out.println("Player's location: " + p.x + ", " + p.y);
	}
	
	private void updateBackupServer() {
		if(players.size() >= 2) {
			String playerName = (!players.firstElement().name.equals(this.playerName)) ? players.firstElement().name : players.get(1).name;
			
			try {
				GameRMI gRMI = (GameRMI) registry.lookup(playerName);
				gRMI.updateGameState(players, treasures);
			} catch (Exception e) {
				System.out.println("updateBackupServer failed: Failed to updateBackupPlayer.");
			}
		}
	}
	
	private boolean hasCollisionWithPlayer(int posX, int posY) {
		// Check for collision with every other player
		boolean hasCollided = false;
		
		for(Player p : players) {
			if(posX == p.x && posY == p.y) {
				hasCollided = true;
				System.out.println("Has collision with Player " + p.name);
				break;
			}
		}
		
		return hasCollided;
	}
	
	private void foundTreasure(Player player) {
		// O(K*P)
		for(Treasure t : treasures) {
			if(player.x == t.x && player.y == t.y) {
				// Update player's score
				player.score++;
				
				// Set new location for treasure
				Position pos = pickNewPosition();
				
				t.x = pos.x;
				t.y = pos.y;
				
				System.out.println("Score: " + player.score);
				
				break;
			}
		}
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
			if(c == '0' || c == '1' || c == '2' || c == '3' || c == '4') {				
				// Update game state if not server
				if(Game.players.firstElement().name.equals(playerName)) {
					g.makeMove(c, playerName);
				} else {
					g.contactServerToMove(c);
				}
				
				g.panel.repaint();
			}
			
			// Get next user input
			c = s.next().charAt(0);
		}
		
		// Close scanner
		s.close();
		
		g.isRunning = false;
		
		// Notify tracker to quit game
		// leaveGame() implicitly notifies Tracker to remove player from its list
		if(Game.players.firstElement().name.equals(playerName)) {
			// Remove self from players
			g.leaveGame(playerName);
			
			// Assign new server
			g.assignNewServer();
		} else {
			// RMI server to quit game
			g.requestToLeaveGame();
		}
		
		// Exit program
		System.out.println("Exiting program...");
		System.exit(0);	
	}
}
