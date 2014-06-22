package msdingfield.easyflow.reflect;

import java.lang.reflect.Field;

import msdingfield.easyflow.annotations.Aggregate;
import msdingfield.easyflow.annotations.Output;

public class FieldOperationOutputPort extends FieldOperationPort implements OperationOutputPort {

	public FieldOperationOutputPort(final Field field) {
		super(field);
	}
	
	@Override
	public Object get(final Object instance) throws IllegalArgumentException, IllegalAccessException {
		return field.get(instance);
	}

	@Override
	public boolean aggregate() {
		return field.isAnnotationPresent(Aggregate.class);
	}

	@Override
	public String getName() {
		final Output output = field.getAnnotation(Output.class);
		final String port = output.port();
		return port == null || "".equals(port) ? getFieldName() : port;
	}

	@Override
	public String toString() {
		return "FieldOperationOutputPort [field=" + field + ", aggregate()="
				+ aggregate() + ", getName()=" + getName();
	}

}
