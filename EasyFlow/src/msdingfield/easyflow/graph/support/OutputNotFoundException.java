package msdingfield.easyflow.graph.support;

/** Exception thrown when a required output could not be found. */
public class OutputNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5821094888654021383L;

	public OutputNotFoundException() {
		/* empty */
	}

	public OutputNotFoundException(final String msg) {
		super(msg);

	}

	public OutputNotFoundException(final Throwable cause) {
		super(cause);

	}

	public OutputNotFoundException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
