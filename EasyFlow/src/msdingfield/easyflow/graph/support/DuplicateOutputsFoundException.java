package msdingfield.easyflow.graph.support;

/** Exception thrown when multiple FlowNodes produce the same output. */
public class DuplicateOutputsFoundException extends RuntimeException {

	private static final long serialVersionUID = -8474909123067668232L;

	public DuplicateOutputsFoundException() {
		/* empty */
	}

	public DuplicateOutputsFoundException(final String message) {
		super(message);
	}

	public DuplicateOutputsFoundException(final Throwable cause) {
		super(cause);
	}

	public DuplicateOutputsFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
