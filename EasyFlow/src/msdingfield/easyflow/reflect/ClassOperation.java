package msdingfield.easyflow.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;

/** Defines an operation which transforms inputs to outputs.
 * 
 * A ClassOperation can only be invoked in the context of a Task.
 * 
 * Instances of ClassOperation are immutable.  All state is held in a Context
 * object provided by the invoker.  When invoked, one or more instances of the
 * class are created and the inputs populated from the context.  When complete
 * the outputs are written back into the context.
 * 
 * A very important implementation consideration is that the operation need not
 * execute synchronously.  It may fork any number of child tasks.  This class
 * must wait until all such child tasks have completed before copying outputs
 * back into the context.
 * 
 * NOTE: The dependency on Task is more than a little awkward.  This class and
 * the Task class could stand to be refactored.  It might be more clear if we
 * had a subclass of Task specific to executing a ClassOperation.
 * 
 * @author Matt
 *
 */
public class ClassOperation {
	
	/** Class containing the operation method and input/output. */
	private final Class<?> operationClass;
	
	/** The constructor to use when creating instances of operationClass. */
	private final Constructor<?> constructor;
	
	/** The instance method to invoke to transform input to output. */
	private final Method operationMethod;
	
	/** The inputs to the operation. */
	private final Set<OperationInputPort> inputs;
	
	/** The outputs from the operation. */
	private final Set<OperationOutputPort> outputs;

	/** Construct instance from a nested builder instance. */
	protected ClassOperation(final Builder builder) {
		this.operationClass = builder.operationClass;
		this.constructor = builder.constructor;
		this.operationMethod = builder.operationMethod;
		this.inputs = builder.inputs;
		this.outputs = builder.outputs;
		validate();
	}

	/**
	 * This runs before the operation to create instances and inject inputs.
	 * 
	 * @param context
	 */
	public void before(final Context context) {
		try {
			final List<Object> instances = Lists.newArrayList();
			context.setStateValue(operationClass, instances);
			final OperationInputPort forkOn = getForkList(context);
			if (forkOn == null) {
				newInstance(context, instances);
			} else {
				final Object forkValue = context.getEdgeValue(forkOn.getConnectedEdgeName());
				forkInit(context, forkValue, forkOn, instances);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Invoke operation method asynchronously.
	 * 
	 * @param context
	 */
	public void execute(final Context context) {
		try {
			@SuppressWarnings("unchecked")
			final Collection<Object> instances = (Collection<Object>) context.getStateValue(operationClass);
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
	
	/**
	 * This runs after all operations are complete to copy outputs back into 
	 * context.
	 * 
	 * @param context
	 */
	public void after(final Context context) {
		try {
			@SuppressWarnings("unchecked")
			final Collection<Object> instances = (Collection<Object>)context.getStateValue(operationClass);
			for (final Object instance : instances) {
				saveOutputsToContext(context, instance, null != getForkList(context));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void forkInit(final Context context, final Object forkValue, final OperationInputPort forkValueSetter, final List<Object> instances) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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
		for (final OperationInputPort setter : inputs) {
			if (!setter.fork()) {
				setInput(context, instance, setter);
			}
		}
		return instance;
	}

	private OperationInputPort getForkList(final Context context) {
		for (final OperationInputPort setter : inputs) {
			if (setter.fork()) {
				return setter;
			}
		}
		return null;
	}

	private void setInput(final Context context,
			final Object instance, final OperationInputPort setter)
			throws IllegalAccessException {
		
		final Object attribute = context.getEdgeValue(setter.getConnectedEdgeName());
		setInput(instance, setter, attribute);
	}

	private void setInput(final Object instance, final OperationInputPort setter,
			final Object attribute) throws IllegalAccessException {
		if (attribute instanceof ListenableFuture) {
			setFromFuture(instance, setter, (ListenableFuture<?>)attribute);
		} else {
			setFromValue(instance, setter, attribute);
		}
	}

	private void setFromFuture(
			final Object instance,
			final OperationInputPort setter, 
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
	
	private void setFromValue(final Object instance, final OperationInputPort setter, final Object attribute) throws IllegalArgumentException, IllegalAccessException
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
		for (final OperationOutputPort getter : outputs) {
			if (getter.aggregate()) {
				Collection<Object> values = getOrCreateCollection(context, getter.getConnectedEdgeName());
				final Object value = getter.get(instance);
				values.add(value);
			} else if(gatherAll) {
				Collection<Object> values = getOrCreateCollection(context, getter.getConnectedEdgeName());
				final Object value = getter.get(instance);
				values.add(value);
			} else {
				final Object value = getter.get(instance);
				context.setEdgeValue(getter.getConnectedEdgeName(), value);
			}
		}
	}
	
	private Collection<Object> getOrCreateCollection(final Context context, final String name) {
		synchronized (context) {
			@SuppressWarnings("unchecked")
			Collection<Object> values = (Collection<Object>) context.getEdgeValue(name);
			if (values == null) {
				values = new Aggregation<Object>();
				context.setEdgeValue(name, values);
			}
			return values;	
		}
	}
	
	public static class Builder {
		private final Class<?> operationClass;
		private Constructor<?> constructor = null;
		private Method operationMethod = null;
		private final Set<OperationInputPort> inputs = Sets.newHashSet();
		private final Set<OperationOutputPort> outputs = Sets.newHashSet();
		
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
			inputs.add(new FieldOperationInputPort(field));
			return this;
		}
		
		public Builder addOutput(final Field field) {
			outputs.add(new FieldOperationOutputPort(field));
			return this;
		}
		
		public ClassOperation newOperation() {
			final ClassOperation op = new ClassOperation(this);
			return op;
		}
	}

	/** Subclass Vector as marker for values aggregated together by the framework. */
	@SuppressWarnings("serial")
	private static class Aggregation<T> extends Vector<T> {}

	public Set<OperationInputPort> getInputs() {
		return inputs;
	}

	public Set<OperationOutputPort> getOutputs() {
		return outputs;
	}

	private void validate() {
		if (operationClass == null) {
			throw new InvalidOperationBindingException("operationClass cannot be null.");
		}
		
		if (constructor == null) {
			throw new InvalidOperationBindingException("constructor cannot be null.");
		}
		
		if (constructor.getParameterTypes().length > 0) {
			throw new InvalidOperationBindingException("constructor must be default (no arg) constructor.");
		}
		
		if (Modifier.PUBLIC != (constructor.getModifiers() & Modifier.PUBLIC)) {
			throw new InvalidOperationBindingException("constructor must be public.");
		}
		
		if (operationMethod == null) {
			throw new InvalidOperationBindingException("operationMethod cannot be null.");
		}
		
		if (operationMethod.getParameterTypes().length > 0) {
			throw new InvalidOperationBindingException("operationMethod must take an empty parameter list.");
		}
		
		if (Modifier.PUBLIC != (operationMethod.getModifiers() & Modifier.PUBLIC)) {
			throw new InvalidOperationBindingException("operationMethod must be public.");
		}
	}

	@Override
	public String toString() {
		return "ClassOperation [operationClass=" + operationClass
				+ ", constructor=" + constructor + ", operationMethod="
				+ operationMethod + ", inputs=" + inputs + ", outputs="
				+ outputs + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constructor == null) ? 0 : constructor.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result
				+ ((operationClass == null) ? 0 : operationClass.getCanonicalName().hashCode());
		result = prime * result
				+ ((operationMethod == null) ? 0 : operationMethod.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
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
		if (constructor == null) {
			if (other.constructor != null)
				return false;
		} else if (!constructor.equals(other.constructor))
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (operationClass == null) {
			if (other.operationClass != null)
				return false;
		} else if (operationClass != other.operationClass)
			return false;
		if (operationMethod == null) {
			if (other.operationMethod != null)
				return false;
		} else if (!operationMethod.equals(other.operationMethod))
			return false;
		if (outputs == null) {
			if (other.outputs != null)
				return false;
		} else if (!outputs.equals(other.outputs))
			return false;
		return true;
	}

}
