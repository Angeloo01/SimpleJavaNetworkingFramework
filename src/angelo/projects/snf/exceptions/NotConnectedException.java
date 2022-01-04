package angelo.projects.snf.exceptions;

public class NotConnectedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NotConnectedException() {
		super();
	}
	
	public NotConnectedException(String msg) {
		super(msg);
	}

}
