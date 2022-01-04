package angelo.projects.snf;

import java.io.Serializable;

public abstract class AsynchronousOutServer extends Server {

	/**
	 * Creates a server on the specified address and port
	 * 
	 * @param host - address of this server
	 * @param port - A valid port value is between 0 and 65535.A port number of zero
	 *             will let the system pick up an ephemeral port in a bind
	 *             operation.
	 * @throws IllegalArgumentException - when port parameter is outside of the
	 *                                  range of valid port numbers
	 */
	public AsynchronousOutServer(String host, int port) throws IllegalArgumentException {
		super(host, port);
	}

	/**
	 * Creates a server on the localhost and specified port
	 * 
	 * @param port - A valid port value is between 0 and 65535.A port number of zero
	 *             will let the system pick up an ephemeral port in a bind
	 *             operation.
	 * @throws IllegalArgumentException - when port parameter is outside of the
	 *                                  range of valid port numbers
	 */
	public AsynchronousOutServer(int port) throws IllegalArgumentException {
		super(port);
	}
	
	/**Asynchronously calls OnMessageReceived() whenever a message is received
	 *
	 */
	@Override
	public void addIncomingMessage(Serializable message, Connection connection) {
		OnMessageReceived(message, clientIDMap.entrySet().stream().filter(entry -> entry.getValue() == connection).findFirst().get().getKey());
	}
	
	@Override
	public void update(int max) {
		System.out.println("[CLIENT] This is an asynchronous out client, update() is not used.");
	}
	
	

}
