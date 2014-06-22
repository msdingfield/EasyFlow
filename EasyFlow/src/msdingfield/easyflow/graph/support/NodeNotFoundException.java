package msdingfield.easyflow.graph.support;

/**
 * Exception thrown when a requested node is not found.
 * 
 * @author Matt
 *
 */
public class NodeNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 8403309944327234698L;

	public NodeNotFoundException() {
	}

	public NodeNotFoundException(String arg0) {
		super(arg0);
	}

	public NodeNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public NodeNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NodeNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
