import java.io.Serializable;

public class PlayerInfo implements Serializable {
	public String host;
	public int port;
	public String name;
	
	public PlayerInfo(String host, int port, String name) {
		this.host = host;
		this.port = port;
		this.name = name;
	}
}