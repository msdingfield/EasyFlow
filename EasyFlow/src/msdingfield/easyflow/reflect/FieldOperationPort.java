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
}
