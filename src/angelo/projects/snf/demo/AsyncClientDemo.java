package angelo.projects.snf.demo;

import java.io.Serializable;
import java.util.Scanner;

import angelo.projects.snf.AsynchronousOutClient;

public class AsyncClientDemo extends AsynchronousOutClient {

	@Override
	protected void OnConnect() {
		System.out.println("[CLIENT] Connected to server");
		
	}

	@Override
	protected void OnDisconnect() {
		System.out.println("[CLIENT] Disconnected server");
		
	}

	@Override
	protected void OnMessageReceived(Serializable message) {
		if(message instanceof String) {
			System.out.println("[CLIENT] received from server: "+message);
		}
		
	}
	
	public static void main(String[] args) {
		AsyncClientDemo client = new AsyncClientDemo();
		client.connect("localhost", 12345);
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter 'exit' to stop.");
		while(true) {
			String msg = sc.nextLine();
			
			if(msg.equalsIgnoreCase("exit")) {
				break;
			}
			
			client.send(msg);
		}
		sc.close();
		
	}

}
