package msdingfield.easyflow.reflect.support;

/** Exception thrown a ClassOperation is bound incorrectly. */
public class InvalidOperationBindingException extends RuntimeException {

	private static final long serialVersionUID = -5496976085251532940L;

	public InvalidOperationBindingException() {
		super();
	}

	public InvalidOperationBindingException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidOperationBindingException(String message) {
		super(message);
	}

	public InvalidOperationBindingException(Throwable cause) {
		super(cause);
	}

}
