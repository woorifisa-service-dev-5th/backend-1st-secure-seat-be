package ticketProject;

import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;

public class ClientRequest {
	private String clientName;
	private int x;
	private int y;
	private OutputStream out;
	private Socket socket;
	private Timestamp timestamp;
	
	public ClientRequest(String clientName, int x, int y, OutputStream out,  Socket socket, Timestamp timestamp) {
		super();
		this.clientName = clientName;
		this.x = x;
		this.y = y;
		this.out = out;
		this.socket = socket;
		this.timestamp = timestamp;
	}
	
	
	public String getClientName() {
		return clientName;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public OutputStream getOut() {
		return out;
	}

	public Socket getSocket() {
		return socket;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
}
