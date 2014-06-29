package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.reflect.ClassOperation;
import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

/** Builds a ClassOperation for an annotated class. */
public final class AnnotationClassOperationBuilder {

	/**
	 * Creates a ClassOperation for the given class.
	 * 
	 * @param type A class with appropriate annotations.
	 * @return ClassOperation bound to the given class.
	 */
	public static ClassOperationProxy fromClass(final Class<?> type) {
		if (type == null) {
			throw new InvalidOperationBindingException("Failed to build ClassOperation.  Class reference is null.");
		}
		
		final ClassOperation builder = new ClassOperation();
		builder.setOperationClass(type);
		
		builder.setConstructor(findNoArgConstructor(type));
		
		builder.setOperationMethod(findOperationAnnotatedMethod(type));
		
		for (final Field field : type.getFields()) {
			if (field.isAnnotationPresent(Input.class)) {
				builder.addInput(field);
			} else if (field.isAnnotationPresent(Output.class)) {
				builder.addOutput(field);
			}
		}
		
		return new ClassOperationProxy(builder);
	}

	private static Method findOperationAnnotatedMethod(final Class<?> type) {
		for (final Method method : type.getMethods()) {
			if (method.isAnnotationPresent(Operation.class)) {
				return method;
			}
		}
		throw new InvalidOperationBindingException(String.format("Failed to build ClassOperation.  %s does not have a method annotated with @Operation.", type.getSimpleName()));
	}

	private static Constructor<?> findNoArgConstructor(final Class<?> type) {
		for (final Constructor<?> c : type.getConstructors()) {
			if (c.getParameterTypes().length == 0) {
				return c;
			}
		}
		throw new InvalidOperationBindingException(String.format("Failed to build ClassOperation.  %s does not have a default (no arg) constructor.", type.getSimpleName()));
	}
	
	private AnnotationClassOperationBuilder() {}
}
