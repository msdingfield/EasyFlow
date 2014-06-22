package msdingfield.easyflow.graph;

/** Exception thrown when the same output name is used by more than one 
 * operation within the same system.
 */
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
