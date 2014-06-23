package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

/** A base class for Input and Output port implementations. */
public abstract class FieldOperationPort implements OperationPort {

	/** The Field we are wrapping. */
	protected final Field field;
	
	/** For use by concrete implementations.  Wraps a field. */
	protected FieldOperationPort(final Field field) {
		this.field = field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	/** Get the name of the field. */
	public String getFieldName() {
		return field.getName();
	}
	
	/**
	 * Returns the name of the edge to which this port is connected.
	 * Typically the edge and port will share the same name.
	 */
	@Override
	public abstract String getConnectedEdgeName();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldOperationPort other = (FieldOperationPort) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}
}
