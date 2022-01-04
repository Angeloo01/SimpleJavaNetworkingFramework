package angelo.projects.snf.demo;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Scanner;

import angelo.projects.snf.AsynchronousOutServer;

public class AsyncServerDemo extends AsynchronousOutServer {

	public AsyncServerDemo(int port) throws IllegalArgumentException {
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
		AsyncServerDemo server = new AsyncServerDemo(12345);
		server.startServer();
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter 'exit' to stop.");
		while(true) {
			String msg = sc.nextLine();
			
			if(msg.equalsIgnoreCase("exit")) {
				break;
			}
		}
		sc.close();
		
	}

}
