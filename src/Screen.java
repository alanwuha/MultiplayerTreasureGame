import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;

public class Screen extends JPanel {
	
	public Screen() {
		setBackground(Color.white);
		setPreferredSize(new Dimension(Game.width + Game.width / 2, Game.width));
	}
	
	@Override
	public void paint(Graphics g) {		
		// Reset background
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, Game.width / 2, Game.width);
		g.setColor(Color.white);
		g.fillRect(Game.width / 2, 0, Game.width, Game.width);
		
		// Draw treasures
		for(Treasure t : Game.treasures) {
			t.show(g);
		}
		
		// Draw players
		for(Player p : Game.players) {
			p.show(g);
		}
		
		// Draw scoreboard
		Font f = new Font("Arial", Font.BOLD, 25);
		g.setFont(f);
		for(int i = 0; i < Game.players.size(); ++i) {
			// Draw player's rect
			g.setColor(Game.players.get(i).color);
			g.fillRect(Game.cellSize / 2, i * Game.cellSize + (i+1) * Game.cellSize / 2, Game.cellSize, Game.cellSize);
			
			// Draw player's name
			g.setFont(f);
			g.setColor(Color.black);
			g.drawString(Game.players.get(i).name, Game.cellSize / 2 + Game.cellSize / 4 - 6, i * Game.cellSize + (i+1) * Game.cellSize / 2 + Game.cellSize - 8);
			
			// Draw player's score
			g.setColor(Color.white);
			g.drawString(Integer.toString(Game.players.get(i).score), Game.cellSize * 2, i * Game.cellSize + (i+1) * Game.cellSize / 2 + Game.cellSize - 8);
		}
		
		// Draw server information
		f = new Font("Arial", Font.BOLD, 20);
		g.setFont(f);
		g.setColor(Color.lightGray);
		g.drawString("PRI", Game.cellSize * 2, Game.width - Game.cellSize);
		g.drawString("SEC", Game.cellSize * 4, Game.width - Game.cellSize);
		
		// Primary rect
		g.setColor(Game.players.firstElement().color);
		g.fillRect(Game.cellSize * 2, Game.width - Game.cellSize * 3, Game.cellSize, Game.cellSize);
		f = new Font("Arial", Font.BOLD, 25);
		g.setFont(f);
		g.setColor(Color.black);
		g.drawString(Game.players.firstElement().name, Game.cellSize * 2 + 1, Game.width - Game.cellSize * 2 - 8);
		
		// Secondary rect
		if(Game.players.size() > 1) {
			g.setColor(Game.players.get(1).color);
			g.fillRect(Game.cellSize * 4 + 5, Game.width - Game.cellSize * 3, Game.cellSize, Game.cellSize);
			
			g.setColor(Color.black);
			g.drawString(Game.players.get(1).name, Game.cellSize * 4 + 6, Game.width - Game.cellSize * 2 - 8);
		}
	}
}