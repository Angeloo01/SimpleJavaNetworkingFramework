package angelo.projects.snf;

import java.io.Serializable;

public abstract class AsynchronousOutClient extends Client {
	
	public AsynchronousOutClient() {
	}
	
	/**Asynchronously calls OnMessageReceived() whenever a message is received
	 *
	 */
	@Override
	public void addIncomingMessage(Serializable message, Connection connection) {
		OnMessageReceived(message);
	}
	
	@Override
	public void update(int max) {
		System.out.println("[CLIENT] This is an asynchronous out client, update() is not used.");
	}
}
