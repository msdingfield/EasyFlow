package msdingfield.easyflow.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.List;

import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Scope;

import com.google.common.collect.Lists;

/** Helpers for scanning the class path for ClassOperations.
 * 
 * This class uses its own class loader and assumes it was loaded with a
 * URLClassLoader implementation.
 * 
 * @author Matt
 *
 */
public class ClassPathScannerClassOperationBuilder {

	/**
	 * Look for ClassOperations within the given package.
	 * 
	 * This limits excludes classes not annotated with the requested scope.
	 * 
	 * This will search for top level and static inner classes.  Class must
	 * have @Scope annotation and have one method with @Operation annotation.
	 * 
	 * @param basePkg Java package to scan.
	 * @param scope The scope to load.
	 * @return List of ClassOperations.
	 */
	public static List<ClassOperation> loadOperationsOnClasspath(
			final String basePkg, final String scope) {
		final List<ClassOperation> operations = Lists.newArrayList();

		final URLClassLoader classLoader = (URLClassLoader) ClassPathScannerClassOperationBuilder.class.getClassLoader();
		loadFrom(ClassScanner.from(classLoader, basePkg), scope, operations);
		return operations;
	}

	private static void loadFrom(final Iterable<Class<?>> scanner, final String scope,
			final List<ClassOperation> operations) {
		for (final Class<?> type : scanner) {
			loadFrom(ClassScanner.from(type), scope, operations);

			if (isStaticClass(type) && isInScope(type, scope) && ClassPathScannerClassOperationBuilder.isAnnotatedOperationClass(type)) {
				final ClassOperation op = AnnotationClassOperationBuilder.fromClass(type);
				operations.add(op);
			}
		}
	}

	private static boolean isStaticClass(final Class<?> type) {
		final int modifiers = type.getModifiers();
		final boolean isStatic = (modifiers & Modifier.STATIC) != 0;
		final boolean isTopmost = type.getEnclosingClass() == null;
		return isStatic || isTopmost;
	}

	private static boolean isInScope(final Class<?> type, final String scope) {
		if (scope == null) {
			return true;
		}

		if (!type.isAnnotationPresent(Scope.class)) {
			return false;
		}

		final Scope scopeAnnotation = type.getAnnotation(Scope.class);
		if (scope.equals(scopeAnnotation.value())) {
			return true;
		}

		return false;
	}

	private static boolean isAnnotatedOperationClass(final Class<?> type) {
		final Method[] methods = type.getDeclaredMethods();
		for (final Method method : methods) {
			final Operation annotation = method.getAnnotation(Operation.class);
			if (annotation != null) {
				return true;
			}
		}
		return false;
	}

}
