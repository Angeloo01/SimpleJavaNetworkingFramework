package angelo.projects.snf;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import angelo.projects.snf.tools.Pair;

public abstract class Client implements ConnectionParent{
	private ConcurrentLinkedQueue<Pair<Serializable, Connection>> messageIn;
	private Connection connection;
	
	//inherited methods
	/**Called when this has successfully connected to the server*/
	protected abstract void OnConnect();
	/**Called when this has disconnected to the server*/
	protected abstract void OnDisconnect();
	/**Called when a message is received (update() must be called)*/
	protected abstract void OnMessageReceived(Serializable message);
	
	public Client() {
	}
	
	/**Returns the status of connection
	 * @return true if client is connected, false otherwise
	 */
	public boolean isConnected() {
		if(connection == null) return false;
		return connection.isConnected();
	}
	
	/**Disconnects the client's connection and calls the OnDisconnect() function
	 * 
	 */
	public void disconnect() {
		if(isConnected()) {
			connection.disconnect();
		}
		OnDisconnect();
	}
	
	/**Connects the client's connection to the specified address and calls the OnConnect() function when successful
	 * @param host - address of the host system
	 * @param port - port the server is connected to
	 * @return true if connection was successful, false otherwise
	 */
	public boolean connect(String host, int port) {
		messageIn = new ConcurrentLinkedQueue<Pair<Serializable, Connection>>();
		try {
			connection = new Connection(AsynchronousSocketChannel.open(), this);
			connection.connectToServer(new InetSocketAddress(host, port));
		} catch (Exception e) {
			System.err.println("\nError in connecting to server...");
			e.printStackTrace();
			return false;
		}
		if(connection.isConnected()) {
			OnConnect();
			return true;
		}
		else {
			return false;
		}
	}
	
	/**Send a Serializable object to server
	 * @param message - Serializable object to send to server
	 */
	public void send(Serializable message) {
		if(isConnected()) connection.send(message);
		else disconnect();
	}
	
	
	/**This function must be called to process the received messages and call the OnMessageReceived() function
	 * @param max - the maximum number of messages to be processed
	 */
	public void update(int max) {
		Iterator<Pair<Serializable, Connection>> itr = messageIn.iterator();
		
		int processed = 0;
		while(itr.hasNext() && processed < max) {
			OnMessageReceived(itr.next().getFirst());
			itr.remove();
			processed++;
		}
	}
	
	@Override
	public void addIncomingMessage(Serializable message, Connection connection) {
		messageIn.add(new Pair<Serializable, Connection>(message, connection));
		
	}
	
}
