package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.reflect.ClassOperation.Builder;

public final class AnnotationClassOperationBuilder {

	public static ClassOperation fromClass(final Class<?> type) {
		Builder builder = new ClassOperation.Builder(type);
		for (final Constructor<?> c : type.getConstructors()) {
			if (c.getParameterTypes().length == 0) {
				builder.setConstructor(c);
				break;
			}
		}
		
		for (final Method method : type.getMethods()) {
			if (method.isAnnotationPresent(Operation.class)) {
				builder.setOperationMethod(method);
				break;
			}
		}
		
		for (final Field field : type.getFields()) {
			if (field.isAnnotationPresent(Input.class)) {
				builder.addInput(field);
			} else if (field.isAnnotationPresent(Output.class)) {
				builder.addOutput(field);
			}
		}
		
		return builder.newOperation();
	}
	
	private AnnotationClassOperationBuilder() {}
}
