import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

public class Player implements Serializable {
	public String host;
	public int port;
	public String name;
	public int x;
	public int y;
	public int score;
	public Color color;
	
	public Player() {};
	
	public Player(String host, int port, String name, int x, int y, int score, Color color) {
		this.host = host;
		this.port = port;
		this.name = name;
		this.x = x;
		this.y = y;
		this.score = 0;
		this.color = color;
	}
	
	public void show(Graphics g) {
		// Draw rect
		g.setColor(this.color);
		g.fillRect(this.x * Game.cellSize + Game.width / 2, this.y * Game.cellSize, Game.cellSize, Game.cellSize);
		
		// Draw string
		Font f = new Font("Arial", Font.BOLD, 25);
		g.setFont(f);
		g.setColor(Color.black);
		g.drawString(name, this.x * Game.cellSize + Game.cellSize / 4 - 5 + Game.width / 2, this.y * Game.cellSize + Game.cellSize - 10);
	}
}