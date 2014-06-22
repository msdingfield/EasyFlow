package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.execution.Task;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

public class ClassOperation {
	private final Class<?> operationClass;
	private final Constructor<?> constructor;
	private final Method operationMethod;
	private final Collection<OperationOutputPort> inputs;
	private final Collection<OperationInputPort> outputs;

	protected ClassOperation(final Builder builder) {
		this.operationClass = builder.operationClass;
		this.constructor = builder.constructor;
		this.operationMethod = builder.operationMethod;
		this.inputs = builder.inputs;
		this.outputs = builder.outputs;
	}

	public void before(final Context context) {
		try {
			final List<Object> instances = Lists.newArrayList();
			context.putPortValue(operationClass, instances);
			final OperationOutputPort forkOn = getForkList(context);
			if (forkOn == null) {
				newInstance(context, instances);
			} else {
				final Object forkValue = context.getPortValue(forkOn.getName());
				forkInit(context, forkValue, forkOn, instances);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void execute(final Context context) {
		try {
			@SuppressWarnings("unchecked")
			final Collection<Object> instances = (Collection<Object>) context.getPortValue(operationClass);
			for (final Object instance : instances) {
				Task.fork(new Runnable(){
					@Override public void run() {
						try {
							operationMethod.invoke(instance);
						} catch (IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}});
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void after(final Context context) {
		try {
			@SuppressWarnings("unchecked")
			final Collection<Object> instances = (Collection<Object>)context.getPortValue(operationClass);
			for (final Object instance : instances) {
				saveOutputsToContext(context, instance, null != getForkList(context));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void forkInit(final Context context, final Object forkValue, final OperationOutputPort forkValueSetter, final List<Object> instances) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (forkValue instanceof Collection) {
			@SuppressWarnings("unchecked")
			final Collection<Object> fork = (Collection<Object>) forkValue;
			for (final Object ob : fork) {
				final Object instance = newInstance(context, instances);
				setInput(instance, forkValueSetter, ob);
			}
		} else if (forkValue instanceof ListenableFuture) {
			final ListenableFuture<?> future = (ListenableFuture<?>) forkValue;
			Task.fork(future, new Runnable(){

				@Override
				public void run() {
					try {
						forkInit(context, future.get(), forkValueSetter, instances);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}});
		} else {
			forkInit(context, Collections.singleton(forkValue), forkValueSetter, instances);
		}
	}

	private Object newInstance(final Context context,
			final List<Object> instances) throws InstantiationException,
			IllegalAccessException, InvocationTargetException {
		final Object instance = constructor.newInstance();
		instances.add(instance);
		for (final OperationOutputPort setter : inputs) {
			if (!setter.fork()) {
				setInput(context, instance, setter);
			}
		}
		return instance;
	}

	private OperationOutputPort getForkList(final Context context) {
		for (final OperationOutputPort setter : inputs) {
			if (setter.fork()) {
				return setter;
			}
		}
		return null;
	}

	private void setInput(final Context context,
			final Object instance, final OperationOutputPort setter)
			throws IllegalAccessException {
		
		final Object attribute = context.getPortValue(setter.getName());
		setInput(instance, setter, attribute);
	}

	private void setInput(final Object instance, final OperationOutputPort setter,
			final Object attribute) throws IllegalAccessException {
		if (attribute instanceof ListenableFuture) {
			setFromFuture(instance, setter, (ListenableFuture<?>)attribute);
		} else {
			setFromValue(instance, setter, attribute);
		}
	}

	private void setFromFuture(
			final Object instance,
			final OperationOutputPort setter, 
			final ListenableFuture<?> future) {
		Task.fork(future, new Runnable(){
			@Override public void run() {
				try {
					setFromValue(instance, setter, future.get());
				} catch (IllegalArgumentException | IllegalAccessException
						| InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});
	}
	
	private void setFromValue(final Object instance, final OperationOutputPort setter, final Object attribute) throws IllegalArgumentException, IllegalAccessException
	{
		if (attribute instanceof Aggregation) {
			final Aggregation<?> inAgg = (Aggregation<?>) attribute;
			final Aggregation<Object> outAgg = new Aggregation<Object>();
			for (final Object ob : inAgg) {
				if (ob instanceof ListenableFuture) {
					final ListenableFuture<?> future = (ListenableFuture<?>) ob;
					Task.fork(future, new Runnable(){
						@Override public void run() {
							try {
								outAgg.add(future.get());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}});
				} else {
					outAgg.add(ob);
				}
			}
			setter.set(instance, outAgg);
		} else {
			setter.set(instance, attribute);
		}
	}
	
	private void saveOutputsToContext(final Context context,
			final Object instance, final boolean gatherAll) throws IllegalAccessException {
		for (final OperationInputPort getter : outputs) {
			if (getter.aggregate()) {
				Collection<Object> values = getOrCreateCollection(context, getter.getName());
				final Object value = getter.get(instance);
				values.add(value);
			} else if(gatherAll) {
				Collection<Object> values = getOrCreateCollection(context, getter.getName());
				final Object value = getter.get(instance);
				values.add(value);
			} else {
				final Object value = getter.get(instance);
				context.putPortValue(getter.getName(), value);
			}
		}
	}
	
	private Collection<Object> getOrCreateCollection(final Context context, final String name) {
		synchronized (context) {
			@SuppressWarnings("unchecked")
			Collection<Object> values = (Collection<Object>) context.getPortValue(name);
			if (values == null) {
				values = new Aggregation<Object>();
				context.putPortValue(name, values);
			}
			return values;	
		}
	}
	
	public static class Builder {
		private final Class<?> operationClass;
		private Constructor<?> constructor = null;
		private Method operationMethod = null;
		private final Collection<OperationOutputPort> inputs = Lists.newArrayList();
		private final Collection<OperationInputPort> outputs = Lists.newArrayList();
		
		public Builder(final Class<?> operationClass) {
			this.operationClass = operationClass;
		}
		
		public Builder setOperationMethod(final Method operationMethod) {
			this.operationMethod = operationMethod;
			return this;
		}
		
		public Builder setConstructor(final Constructor<?> constructor) {
			this.constructor = constructor;
			return this;
		}
		
		public Builder addInput(final Field field) {
			inputs.add(new FieldOperationOutputPort(field));
			return this;
		}
		
		public Builder addOutput(final Field field) {
			outputs.add(new FieldOperationInputPort(field));
			return this;
		}
		
		public ClassOperation newOperation() {
			if (operationMethod == null) {
				throw new MissingOperationException();
			}
			
			final ClassOperation op = new ClassOperation(this);
			return op;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operationClass == null) ? 0 : operationClass.hashCode());
		result = prime * result
				+ ((operationMethod == null) ? 0 : operationMethod.hashCode());
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
		ClassOperation other = (ClassOperation) obj;
		if (operationClass == null) {
			if (other.operationClass != null)
				return false;
		} else if (!operationClass.equals(other.operationClass))
			return false;
		if (operationMethod == null) {
			if (other.operationMethod != null)
				return false;
		} else if (!operationMethod.equals(other.operationMethod))
			return false;
		return true;
	}

	/** Subclass Vector as marker for values aggregated together by the framework. */
	@SuppressWarnings("serial")
	private static class Aggregation<T> extends Vector<T> {}

	public Collection<OperationOutputPort> getInputs() {
		return inputs;
	}

	public Collection<OperationInputPort> getOutputs() {
		return outputs;
	}
}
