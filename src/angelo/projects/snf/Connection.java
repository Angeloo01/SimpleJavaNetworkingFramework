package angelo.projects.snf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import angelo.projects.snf.exceptions.NotConnectedException;

public class Connection {
	//ConcurrentLinkedQueue<Pair<Serializable, Connection>> messageIn; //aggregation - handled by client/server interface
	ConnectionParent parent;
	ConcurrentLinkedQueue<Serializable> messageOut; //composition
	AsynchronousSocketChannel socket;
	
	private CompletionHandler<Integer, ByteBuffer> readHandler = new CompletionHandler<Integer, ByteBuffer>(){

		@Override
		public void completed(Integer result, ByteBuffer attachment) {
			//check if client has disconnected
			if(result < 0) {
				//connection has closed
				disconnect();
				return;
			}
			else {
				//process message
				ByteBuffer buffer = attachment;
				buffer.flip();
				
				try {
					//messageIn.add(new Pair<Serializable, Connection>((Serializable) deserialize(buffer.array()), Connection.this));
					parent.addIncomingMessage((Serializable) deserialize(buffer.array()), Connection.this);
				} catch (ClassNotFoundException e) {
				} catch (IOException e) {
					System.err.println("Error in deserializing received message");
				}
				
				//begin reading again
				read();
			}
			
		}

		@Override
		public void failed(Throwable exc, ByteBuffer attachment) {
			System.out.println("Socket Disconnecting...");
			disconnect();
			
		}
		
	};
	
	private CompletionHandler<Integer, Object> writeHandler = new CompletionHandler<Integer, Object>(){

		@Override
		public void completed(Integer result, Object attachment) {
			//remove the message after successful sending
			messageOut.poll();
			//if outgoing queue has messages, continue to send
			if(!messageOut.isEmpty()) {
				write();
			}
			
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			//disconnect on failed send
			disconnect();
		}
		
	};
	
	/**
	 * @param socket - socket of the connection
	 * @param parent - owner of connection
	 */
	public Connection(AsynchronousSocketChannel socket, ConnectionParent parent) {
		messageOut = new ConcurrentLinkedQueue<Serializable>();
		this.socket = socket;
		this.parent = parent;
		//this.messageIn = messageIn;
	}
	
	/**Begin connection to server. CLIENT side only!
	 * @param address - address of server
	 * @throws Exception - if connection attempt not successful
	 */
	public void connectToServer(InetSocketAddress address) throws Exception{

		socket = AsynchronousSocketChannel.open();
		Future<Void> future = socket.connect(address);
		future.get(60, TimeUnit.SECONDS);
		read();

	}
	
	/**Begin connection to client. SERVER side only!
	 * 
	 */
	public void connectToClient() {
		read();
	}
	
	/**Disconnect the connection
	 * 
	 */
	public void disconnect() {
		if(socket == null || !isConnected()) {
			return;
		}
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/**Returns the status of the connection
	 * @return true if socket is open, false otherwise
	 */
	public boolean isConnected() {
		return socket != null && socket.isOpen();
	}
	
	/**Queues message to prepare sending over socket
	 * @param message - message to send
	 * @throws NotConnectedException - if connection has not been connected
	 */
	public void send(Serializable message) throws NotConnectedException{
		if(!isConnected()) throw new NotConnectedException("Connection is not connected");
		
		boolean writing = !messageOut.isEmpty();
		messageOut.add(message);
		if(!writing) {
			write();
		}
	}
	
	/**Begins asynchronous task of writing messages over the socket
	 * 
	 */
	private void write(){
		
		try {
			//serialize and send message
			socket.write(serialize(messageOut.peek()), null, writeHandler);
		} catch (IOException e) {
			//remove message if error in sending
			System.err.println("\nError in serializing object while writing to connection \nObject: "+messageOut.poll());
		}
	}
	
	/**Begins asynchronous task of reading messages from socket
	 * 
	 */
	private void read(){
		
		ByteBuffer buffer = ByteBuffer.allocate(128);
		socket.read(buffer, buffer, readHandler);
	}
	
	/**Serialize an object to a ByteBuffer
	 * @param obj - Serializazble object to serialize
	 * @return ByteBuffer object containing serialized data
	 * @throws IOException
	 */
	private ByteBuffer serialize(Serializable obj) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		oos.writeObject(obj);
		oos.flush();
		
		ByteBuffer buffer = ByteBuffer.wrap(os.toByteArray());
		
		oos.close();
		os.close();
		
		return buffer;
	}
	
	/**Deserialize a byte array into an Object
	 * @param buffer - byte array to deserialize
	 * @return Object retrieved from byte array
	 * @throws IOException - I/O error occurs while reading stream header
	 * @throws ClassNotFoundException - byte array can not be deserialized into an Object
	 */
	private Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		ByteArrayInputStream is = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(is);
		
		Object obj = ois.readObject();
		
		ois.close();
		is.close();
		
		return obj;
	}
	
	/**Get the remote address the socket is attached to
	 * @return The remote address, null if socket is not connected
	 */
	public SocketAddress getRemoteAddress() {
		if(socket != null)
			try {
				return socket.getRemoteAddress();
			} catch (IOException e) {
				return null;
			}
		return null;
	}
	

}
