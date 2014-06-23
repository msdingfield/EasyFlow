package msdingfield.easyflow.reflect;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Set;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Helper for iterating over all classes, including inner classes, found on the
 * classpath.
 * 
 * Instances of Iterable are return for convenient use in for() statements.
 * 
 * @author Matt
 *
 */
public final class ClassScanner {
	private ClassScanner() {}
	
	/** 
	 * Return an Iterable for all top-level classes within a given Java 
	 * package, including sub-packages.
	 * 
	 * @param classLoader The classLoader to scan.
	 * @param packageName The java package to scan.
	 * @return An Iterable<Class> instance which will return matching classes.
	 */
	public static Iterable<Class<?>> from(final URLClassLoader classLoader, final String packageName) {
		return new TopLevelClassIterable(classLoader, packageName);
	}

	/**
	 * Return an Iterable for all inner classes of the given class.
	 * 
	 * @param type The class to scan.
	 * @return An Iterable<Class> instance which will return matching classes.
	 */
	public static Iterable<Class<?>> from(Class<?> type) {
		return new InnerClassIterable(type);
	}
	
	private static class InnerClassIterable implements Iterable<Class<?>> {
		private final Class<?> outerClass;
		public InnerClassIterable(final Class<?> outerClass) {
			this.outerClass = outerClass;
		}
		
		@Override
		public Iterator<Class<?>> iterator() {
			return new ClassIterator(outerClass.getDeclaredClasses());
		}
		
		private class ClassIterator implements Iterator<Class<?>> {
			private final Class<?>[] classes;
			private int index = 0;
			public ClassIterator(final Class<?>[] classes) {
				this.classes = classes;
			}
			
			@Override
			public boolean hasNext() {
				return index < classes.length;
			}

			@Override
			public Class<?> next() {
				if (hasNext()) {
					final Class<?> current = classes[index++]; 
					return current;
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}
	}

	private static class TopLevelClassIterable implements Iterable<Class<?>> {
		private final ClassLoader loader;
		private final String packageName;
		
		public TopLevelClassIterable(final URLClassLoader loader, final String packageName) {
			this.loader = loader;
			this.packageName = packageName;
		}
	
		@Override
		public Iterator<Class<?>> iterator() {
			try {
				final ClassPath cp = ClassPath.from(loader);
				if (packageName == null) {
					return new ClassIterator(cp.getAllClasses());
				} else {
					return new ClassIterator(cp.getTopLevelClassesRecursive(packageName));
				}
			} catch (IOException e) {
				throw new RuntimeException("Unexpected", e);
			}
		}

		public class ClassIterator implements Iterator<Class<?>> {
			private final Iterator<ClassInfo> infos;
			
			public ClassIterator(final Set<ClassInfo> infos)
			{
				this.infos = infos.iterator();
			}
			
			@Override
			public boolean hasNext() {
				return infos.hasNext();
			}

			@Override
			public Class<?> next() {
				try {
					final ClassInfo info = infos.next();
					final Class<?> type = loader.loadClass(info.getName());
					return type;
				} catch (final ClassNotFoundException e) {
					throw new RuntimeException("Unexpected!", e);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}

	}
	
}
