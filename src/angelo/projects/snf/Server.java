package angelo.projects.snf;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import angelo.projects.snf.tools.Pair;

public abstract class Server implements ConnectionParent{
	private InetSocketAddress serverAddress;
	protected ConcurrentLinkedQueue<Pair<Serializable, Connection>> messageIn;
	protected ConcurrentHashMap<Integer, Connection> clientIDMap;
	protected ConcurrentLinkedQueue<Connection> clients;
	private AsynchronousServerSocketChannel serverSocket;
	private int idCounter = 0;

	/**
	 * Called by the server when a client attempts to connect. The return value
	 * determines if client is accepted. True to accept client, false otherwise.
	 * 
	 * @param client  - the ID assigned the client if it is accepted
	 * @param address - address of the connecting client
	 * @return true to accept the client connection, false otherwise
	 */
	protected abstract boolean OnClientConnect(int client, SocketAddress address);

	/**
	 * Called when a client has disconnected to the server
	 * 
	 * @param client - ID of client that has disconnected
	 */
	protected abstract void OnClientDisconnect(int client);

	/**
	 * Called when a message is received (update() must be called)
	 * 
	 * @param message - Serializable object received
	 * @param client  - client that sent the message
	 */
	protected abstract void OnMessageReceived(Serializable message, int client);

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
	public Server(String host, int port) throws IllegalArgumentException {
		serverAddress = new InetSocketAddress(host, port);
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
	public Server(int port) throws IllegalArgumentException {
		serverAddress = new InetSocketAddress(port);
	}

	/**
	 * Start the server
	 * 
	 * @return true if server started succesfully, false otherwise
	 */
	public boolean startServer() {
		try {
			serverSocket = AsynchronousServerSocketChannel.open();
			serverSocket.bind(serverAddress);

			messageIn = new ConcurrentLinkedQueue<Pair<Serializable, Connection>>();
			clientIDMap = new ConcurrentHashMap<Integer, Connection>();
			clients = new ConcurrentLinkedQueue<Connection>();
			idCounter = 0;

			acceptConnections();
			System.out.println("[SERVER] Server started successfully...");
			return true;
		} catch (IOException e) {
			System.out.println("[SERVER] Server did NOT start...");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Stops and resets the server
	 * 
	 */
	public void stopServer() {
		if (serverSocket == null)
			return;
		try {
			serverSocket.close();
			clients.forEach(connection -> {
				connection.disconnect();
			});
			clients.clear();
			messageIn.clear();
			clientIDMap.clear();
			idCounter = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called to start accepting for connections
	 * 
	 */
	private void acceptConnections() {
		// begin listening for connections
		serverSocket.accept(null, acceptHandler);
	}

	/**
	 * Handler of accepting new conenctions
	 * 
	 */
	private CompletionHandler<AsynchronousSocketChannel, Object> acceptHandler = new CompletionHandler<AsynchronousSocketChannel, Object>() {

		@Override
		public void completed(AsynchronousSocketChannel result, Object attachment) {
			SocketAddress addr = null;
			try {
				addr = result.getRemoteAddress();
			} catch (IOException e) {
				System.err.println("\n[SERVER] Accepting a connection failed...");
				e.printStackTrace();
				try {
					result.close();
				} catch (IOException e1) {
				}
				return;
			}
			if (addr == null)
				return;

			System.out.println("\n[SERVER] Connection attempt from [" + addr + "]...");
			// check if connection is accepted
			if (OnClientConnect(idCounter, addr)) {
				System.out.println("[SERVER] Connection accepted");

				// add to list of clients
				Connection newConn = new Connection(result, Server.this);
				clientIDMap.put(idCounter, newConn);
				clients.add(newConn);

				// begin connection
				newConn.connectToClient();

				idCounter++;
			} else {
				System.out.println("[SERVER] Connection denied");
			}

			// begin listening for connections again
			acceptConnections();
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			System.err.println("\n[SERVER] Accepting a connection failed...");
			System.err.println(exc);

			// continue listening for connections
			acceptConnections();
		}

	};

	/**Sends the message to a client
	 * @param id - ID of the client
	 * @param message - Serializable object to send
	 */
	public void MessageClient(int id, Serializable message) {
		// check if id is valid
		if (!isValidID(id)) {
			System.out.println("\n[SERVER] Invalid client ID.");
			return;
		}

		Connection client = clientIDMap.get(id);
		if (client.isConnected()) {
			client.send(message);
		} else {
			// client has disconnected
			disconnectClient(id);
		}
	}
	
	/**Send the message to all clients except for the given ignored client. Set ignoreId to -1 to send to all clients.
	 * @param message - Serializable object to send to clients
	 * @param ignoreId - ignored client. Set to -1 to send to all clients
	 */
	public void MessageAllClients(Serializable message, int ignoreId) {
		clientIDMap.forEach((id, connection) ->{
			if(id != ignoreId) {
				MessageClient(id, message);
			}
		});
	}

	/**Disconnects a client from the server
	 * @param id - ID of the client
	 */
	public void disconnectClient(int id) {
		// check if id is valid
		if (!isValidID(id)) {
			System.out.println("\n[SERVER] Invalid client ID.");
			return;
		}

		Connection client = clientIDMap.get(id);
		
		//call user generated code
		OnClientDisconnect(id);
		
		//remove client
		clientIDMap.remove(id);
		clients.remove(client);
		
		//disconnect client
		client.disconnect();
		
	}
	
	/**Checks if ID is a client
	 * @param id - ID to check
	 * @return true if ID corresponds to a client, false otherwise
	 */
	public boolean isValidID(int id) {
		return clientIDMap.containsKey(id);
	}
	
	/**This function must be called to process the received messages and call the OnMessageReceived() function
	 * @param max - the maximum number of messages to be processed
	 */
	public void update(int max) {
		Iterator<Pair<Serializable, Connection>> itr = messageIn.iterator();
		
		int processed = 0;
		while(itr.hasNext() && processed < max) {
			Pair<Serializable, Connection> data = itr.next();
			//find the id of the connection object and process the message
			OnMessageReceived(data.getFirst(), clientIDMap.entrySet().stream().filter(entry -> entry.getValue() == data.getSecond()).findFirst().get().getKey());
			itr.remove();
			processed++;
		}
	}
	
	@Override
	public void addIncomingMessage(Serializable message, Connection connection) {
		messageIn.add(new Pair<Serializable, Connection>(message, connection));
		
	}

}
