package msdingfield.easyflow.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import msdingfield.easyflow.execution.Task;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * The evaluation time instance of a ClassOperation.
 * 
 * This class can hold multiple instances of the user class if the operation
 * is parallelized.  Each instance of the user class is referred to as an
 * iteration.
 * 
 * When the begin method() is invoked, one or more instances of the user class
 * are created and the operation inputs populated from the context.  When the
 * after() method is invoked, the outputs are written back into the context.
 * 
 * In use, the methods before(), execute() and after() should be called in
 * exactly that order.  Care must be taken because method may initiate
 * asynchronous tasks which continue after method returns.  The next method in
 * the sequence must not be invoked until all of the asynchronous tasks complete

 * @author Matt
 *
 */
public class ClassOperationInstance {

	private final ClassOperation operation;
	private final Context context;
	private final List<OperationIteration> iterations = Lists.newArrayList();

	/** Points to a collection on which to parallelized if this is a parallel operation. */
	private final OperationInputPort forkOn;

	public ClassOperationInstance(final ClassOperation outer, final Context context) {
		this.operation = outer;
		this.forkOn = getForkInput();

		// Bind this instance to the context
		this.context = context;
		context.setStateValue(operation.getOperationClass(), this);
	}

	/**
	 * Create instances of operation class and populate inputs from context.
	 * 
	 * NOTE: This method MAY run asynchronously if incoming edges have
	 * ListenableFuture<> values.  This is required in order to avoid blocking.
	 * 
	 * Must only be executed once on a given ClassOperationInstance.
	 * 
	 * Must not be invoked after execute() is invoked.
	 * 
	 * Must be invoked within a Task context.
	 */
	public void before() {
		final Object forkValue
		= isParallelOperation()
		? context.getEdgeValue(forkOn.getConnectedEdgeName())
				: null;
		initializeOperationClassInstances(forkValue);
	}

	/**
	 * Invoke operation method on instances.
	 * 
	 * NOTE: The operation methods are invoke asynchronously.  This is required
	 * in order to avoid blocking when there is more than one operation instance.
	 * 
	 * Must not be invoked until before() and all asynchronous tasks
	 * initiated by before() have completed.
	 * 
	 * Must only be executed once on a given ClassOperationInstance.
	 * 
	 * Must be invoked within a Task context.
	 */
	public void execute() {
		for (final OperationIteration iteration : iterations) {
			Task.fork(new Runnable(){
				@Override public void run() {
					iteration.execute();
				}});
		}
	}

	/**
	 * Copy operation outputs to context.
	 * 
	 * Must not be invoked until execute() and all asynchronous tasks initiated
	 * by execute() have completed.
	 * 
	 * Must only be executed once on a given ClassOperationInstance.
	 */
	public void after() {
		for (final OperationOutputPort port : operation.getOutputs()) {
			final Object value = isParallelOperation() ? aggregateOutput(port) : iterations.get(0).read(port);
			context.setEdgeValue(port.getConnectedEdgeName(), value);
		}
	}

	/**
	 * Aggregate an output from all iterations into a list.
	 * 
	 * @param port The output to aggregate.
	 * @return The aggregated list.
	 */
	private List<Object> aggregateOutput(final OperationOutputPort port) {
		return Lists.transform(iterations, new Function<OperationIteration,Object>() {
			@Override public Object apply(final OperationIteration iteration) {
				return iteration.read(port);
			}});
	}

	/**
	 * Determines if this operation should be parallelized.
	 * @return True for parallel operation.
	 */
	private boolean isParallelOperation() {
		return forkOn != null;
	}

	/**
	 * Initialize 1 or more operation class instances.
	 * 
	 * If forkValue is Collection<> then an instance will be created for each
	 * entry in the collection.  If the operation class has an input marked as
	 * the fork attribute, then that input on each of the operation instances
	 * will receive a value from the collection.
	 * 
	 * If forkValue is a ListenableFuture<>, then this will asynchronously
	 * wait the future to be ready and then invoke itself recursively with
	 * the resulting value as forkValue.
	 * 
	 * @param forkValue
	 */
	private void initializeOperationClassInstances(final Object forkValue) {
		if (forkValue == null) {
			initializeOneOperationClassInstance(null);
		} else if (forkValue instanceof ListenableFuture) {
			initializeForkOnFuture((ListenableFuture<?>) forkValue);
		} else if(forkValue instanceof Collection) {
			initializeForkOnCollection((Collection<?>) forkValue);
		} else {
			initializeForkOnCollection(Collections.singleton(forkValue));
		}
	}

	/**
	 * Initialize an operation class instance for each element in collection.
	 * @param collection
	 */
	private void initializeForkOnCollection(final Collection<?> collection) {
		for (final Object ob : collection) {
			initializeOneOperationClassInstance(ob);
		}
	}

	/**
	 * Asynchronously initialize class instances using value from future.
	 * @param future
	 */
	private void initializeForkOnFuture(final ListenableFuture<?> future) {
		Task.fork(future, new Runnable(){
			@Override public void run() {
				try {
					initializeOperationClassInstances(Uninterruptibles.getUninterruptibly(future));
				} catch (final ExecutionException e) {
					throw new Task.FatalErrorException("User code aborted with exception while reading Future<> edge value.", e);
				}
			}});
	}

	/**
	 * Initialize one operation class instances.
	 * If the class has an input marked as the fork attribute, then it will
	 * receive the value provided in 'forkValue' rather than a value from
	 * the context.
	 * 
	 * @param forkValue Value to be provided to the fork input.
	 */
	private void initializeOneOperationClassInstance(final Object forkValue) {
		final OperationIteration iteration = new OperationIteration();
		iterations.add(iteration);
		for (final OperationInputPort setter : operation.getInputs()) {
			final Object attribute
			= setter.fork()
			? forkValue : context.getEdgeValue(setter.getConnectedEdgeName());
			iteration.writeVariant(setter, attribute);
		}
	}

	/**
	 * Gets the input marked as the fork attribute.
	 * @return The InputPort if fork input exists.
	 */
	private OperationInputPort getForkInput() {
		for (final OperationInputPort setter : operation.getInputs()) {
			if (setter.fork()) {
				return setter;
			}
		}
		return null;
	}

	/**
	 * Utility for asynchronously expanding futures in a collection.
	 * 
	 * Creates a ListenableFuture which will produce a list as described below.
	 * 
	 * Given a collection of object, some of which are ListenableFutures,
	 * asynchronously wait for the futures to complete and merge the results
	 * with the other values into a single list.
	 * 
	 * @param collection The mixed collection of future and non-future objects.
	 * @return A future which returns a list containing only non-future objects.
	 */
	@SuppressWarnings("unchecked")
	private static ListenableFuture<List<Object>> unwindTopLevelFuturesIfAny(
			final Collection<?> collection) {
		final List<ListenableFuture<Object>> futures = Lists.newArrayList();
		final List<Object> values = Lists.newArrayList();
		for (final Object ob : collection) {
			if (ob instanceof ListenableFuture) {
				futures.add((ListenableFuture<Object>) ob);
			} else {
				values.add(ob);
			}
		}

		final ListenableFuture<List<Object>> combinedFuture = new ListWithFuturesMasherFuture(values, futures);
		return combinedFuture;
	}

	/**
	 * This is a helper class for the method unwindTopLevelFuturesIfAny.
	 * Note that this does not correctly handle cancellation.
	 * @author Matt
	 *
	 */
	private static class ListWithFuturesMasherFuture extends AbstractFuture<List<Object>> {
		public ListWithFuturesMasherFuture(final List<Object> nonFutureList, final List<ListenableFuture<Object>> futureList) {
			if (futureList.size() == 0) {
				set(nonFutureList);
			} else {
				final ListenableFuture<List<Object>> combinedFuture = Task.combineFutures(futureList);
				Task.fork(combinedFuture, new Runnable(){
					@Override public void run() {
						try {
							final List<Object> all = Lists.newArrayList();
							all.addAll(nonFutureList);
							all.addAll(Uninterruptibles.getUninterruptibly(combinedFuture));
							set(all);
						} catch (final Exception e) {
							setException(e);
							throw new Task.FatalErrorException("Error getting values from collection of futures.", e);
						}
					}});
			}
		}
	}

	/**
	 * Wrap the user level operation class instance.
	 * 
	 * An operation may be parallelized on some collection in which case we
	 * have one instance of the user level operation class for each iteration.
	 * 
	 * @author Matt
	 *
	 */
	private final class OperationIteration {
		/** Instance of user level operation class. */
		private final Object object;

		/** Create a new user level operation class instance. */
		public OperationIteration() {
			try {
				this.object = operation.getConstructor().newInstance();
			} catch (IllegalAccessException|InstantiationException e) {
				throw new Task.FatalErrorException("Failed to create instance of operation class.", e);
			} catch (final InvocationTargetException e) {
				throw new Task.FatalErrorException("User exception in operation constructor.", e.getCause());
			}
		}

		/** Invoke the operation method of the user class. */
		public void execute() {
			try {
				call(operation.getOperationMethod());
			} catch (IllegalAccessException|IllegalArgumentException e) {
				throw new Task.FatalErrorException("Internal error while invoking operation.", e);
			} catch (final InvocationTargetException e) {
				throw new Task.FatalErrorException("User operation threw exception.", e.getCause());
			}
		}

		private void call(final Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			method.invoke(object);
		}


		/**
		 * Read value from the output of an operation class instance.
		 * 
		 * Wrap OutputPort.get() converting exceptions.
		 * 
		 * @param instance The operation class instance.
		 * @param port The output to read.
		 * @return The value read.
		 */
		public Object read(final OperationOutputPort port) {
			try {
				return port.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new Task.FatalErrorException("Error reading output port.", e);
			}
		}

		/**
		 * Write value to the input of an operation class instance.
		 * 
		 * @param instance The operation class instance.
		 * @param port The port to write.
		 * @param value The value to write.
		 */
		public void write(final OperationInputPort port, final Object value) {
			try {
				port.set(object, value);
			} catch (final IllegalAccessException e) {
				throw new Task.FatalErrorException("Error setting value on input port.", e);
			}
		}

		/**
		 * Write an unknown variant type to port.
		 * 
		 * This operation may complete asynchronously.
		 * 
		 * @param port The input port to write.
		 * @param variant The unknown object.
		 */
		public void writeVariant(final OperationInputPort port,
				final Object variant) {
			if (variant instanceof ListenableFuture) {
				writeFuture(port, (ListenableFuture<?>)variant);
			} else if (variant instanceof Collection) {
				writeCollection(port, (Collection<?>)variant);
			} else {
				write(port, variant);
			}
		}

		/**
		 * Write a collection to a port.
		 * 
		 * This operation looks for ListenableFutures that are immediate
		 * members of the collection and asynchronously waits for any to
		 * complete.  The resulting collection with no top-level futures
		 * is then written to the input.  This does not attempt to recurse
		 * down to find nested futures.
		 * 
		 * This operation may complete asynchronously.
		 * 
		 * @param port The input port to write.
		 * @param collection The collection to write.
		 */
		public void writeCollection(final OperationInputPort port, final Collection<?> collection) {
			final ListenableFuture<List<Object>> combinedFuture = unwindTopLevelFuturesIfAny(collection);
			Task.fork(combinedFuture, new Runnable() {
				@Override public void run() {
					try {
						write(port, Uninterruptibles.getUninterruptibly(combinedFuture));
					} catch (final ExecutionException e) {
						throw new Task.FatalErrorException("Error setting input.  Collection contained failed futures.", e);
					}
				}});
		}

		/**
		 * Asynchronously writes the value returned by a future to a port.
		 * 
		 * If the future returns another future, the operation will repeat
		 * recursively until a non ListenableFuture type is retrieved.
		 * 
		 * This operation completes asynchronously.
		 * 
		 * @param port The input port to write.
		 * @param future The future to supply the value.
		 */
		public void writeFuture(
				final OperationInputPort port,
				final ListenableFuture<?> future) {
			Task.fork(future, new Runnable(){
				@Override public void run() {
					try {
						writeVariant(port, Uninterruptibles.getUninterruptibly(future));
					} catch (final ExecutionException e) {
						throw new Task.FatalErrorException("Error setting input.  Future failed.", e);
					}
				}});
		}

	}

}