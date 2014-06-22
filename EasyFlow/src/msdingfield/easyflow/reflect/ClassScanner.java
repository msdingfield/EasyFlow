package msdingfield.easyflow.reflect;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Set;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public final class ClassScanner {
	private ClassScanner() {}
	
	public static Iterable<Class<?>> from(final URLClassLoader loader, final String packageName) {
		return new ClassPathScanner(loader, packageName);
	}

	public static Iterable<Class<?>> from(Class<?> type) {
		return new NestedClassScanner(type);
	}
	
	public static class NestedClassScanner implements Iterable<Class<?>> {
		private final Class<?> outerClass;
		public NestedClassScanner(final Class<?> outerClass) {
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

	public static class ClassPathScanner implements Iterable<Class<?>> {
		private final ClassLoader loader;
		private final String packageName;
		
		public ClassPathScanner(final URLClassLoader loader) {
			this.loader = loader;
			this.packageName = null;
		}
	
		public ClassPathScanner(final URLClassLoader loader, final String packageName) {
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
