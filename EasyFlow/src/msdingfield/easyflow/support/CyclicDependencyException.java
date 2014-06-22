package msdingfield.easyflow.support;

/** Exception thrown if a cyclic dependency is detected. */
public class CyclicDependencyException extends RuntimeException {

	private static final long serialVersionUID = -7458331831085619578L;

	public CyclicDependencyException() {
		/* empty */
	}

	public CyclicDependencyException(String msg) {
		super(msg);
	}

	public CyclicDependencyException(Throwable cause) {
		super(cause);
	}

	public CyclicDependencyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
