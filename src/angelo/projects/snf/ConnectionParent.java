package angelo.projects.snf;

import java.io.Serializable;

public interface ConnectionParent {
	public abstract void addIncomingMessage(Serializable message, Connection connection);
}
