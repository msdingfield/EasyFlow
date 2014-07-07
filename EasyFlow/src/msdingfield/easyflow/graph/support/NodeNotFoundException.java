package msdingfield.easyflow.graph.support;

/** Exception thrown when a requested node is not found. */
public class NodeNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 8403309944327234698L;

	public NodeNotFoundException() {
	}

	public NodeNotFoundException(final String message) {
		super(message);
	}

	public NodeNotFoundException(final Throwable cause) {
		super(cause);
	}

	public NodeNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
