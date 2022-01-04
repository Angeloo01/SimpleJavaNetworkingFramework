package angelo.projects.snf.demo;

import java.io.Serializable;
import java.net.SocketAddress;

import angelo.projects.snf.Server;

public class ServerDemo extends Server {

	public ServerDemo(int port) throws IllegalArgumentException {
		super(port);
	}

	@Override
	protected boolean OnClientConnect(int client, SocketAddress address) {
		return true;
	}

	@Override
	protected void OnClientDisconnect(int client) {
		System.out.println("[SERVER] client has disconnected. ID: ["+client+"]");
		
	}

	@Override
	protected void OnMessageReceived(Serializable message, int client) {
		System.out.println("[SERVER] message received from ID: ["+client+"]");
		if(message instanceof String) {
			//String str = (String) message;
			MessageAllClients(message, client);
		}
		
	}
	
	public static void main(String[] args) {
		ServerDemo server = new ServerDemo(12345);
		server.startServer();
		
		while(true) {
			server.update(5);
		}
		
	}

}
