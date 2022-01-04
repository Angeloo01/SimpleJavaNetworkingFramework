package angelo.projects.snf.demo;

import java.io.Serializable;
import java.util.Scanner;

import angelo.projects.snf.Client;

public class ClientDemo extends Client {

	@Override
	protected void OnConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void OnDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void OnMessageReceived(Serializable message) {
		if(message instanceof String) {
			System.out.println("[CLIENT] received from server: "+message);
		}
		
	}
	
	public static void main(String[] args) {
		ClientDemo client = new ClientDemo();
		client.connect("localhost", 12345);
		Scanner sc = new Scanner(System.in);
		while(true) {
			client.update(5);
			String msg = sc.nextLine();
			
			if(msg.equalsIgnoreCase("exit")) {
				break;
			}
			
			client.send(msg);
		}
		sc.close();
		
	}

}
