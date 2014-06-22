package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

public abstract class FieldOperationPort implements OperationPort {

	protected final Field field;
	protected FieldOperationPort(final Field field) {
		this.field = field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	public String getFieldName() {
		return field.getName();
	}
	
	@Override
	public abstract String getName();

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
