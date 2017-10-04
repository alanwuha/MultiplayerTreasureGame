import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

public class Treasure implements Serializable {
	public int x;
	public int y;
	
	public Treasure() {};
	
	public void show(Graphics g) {
		// Draw rect
		g.setColor(Color.lightGray);
		g.fillRect(x * Game.cellSize + Game.width / 2, y * Game.cellSize, Game.cellSize, Game.cellSize);
		
		// Draw asterisk
		Font f = new Font("Arial", Font.BOLD, 50);
		g.setFont(f);
		g.setColor(Color.darkGray);
		g.drawString("*", x * Game.cellSize + Game.width / 2 + Game.cellSize / 4, y * Game.cellSize + Game.cellSize + Game.cellSize / 2);
	}
}