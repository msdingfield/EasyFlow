package msdingfield.easyflow.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;

import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.reflect.Context;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/** Helper for testing.
 * 
 * Sums all integer inputs and adds a constant value.  Puts result into all integer outputs.
 * 
 * @author Matt
 *
 */
public final class AddOperation implements GraphNode {
	private final String name;
	private final int constant;

	/** Outputs from the operation.
	 * 
	 * Map from output name to output type.
	 */
	protected final Set<String> outputsx = Sets.newHashSet();
	/** Inputs to the operation.
	 * 
	 * Map from input name to input type.
	 */
	protected final Set<String> inputsx = Sets.newHashSet();

	public AddOperation(final AddOperation.Builder builder) {
		for (final String e : builder.inputsx) {
			this.inputsx.add(e);
		}
		for (final String decl : builder.outputsx) {
			this.outputsx.add(decl);
		}
		
		this.name = builder.name;
		this.constant = builder.constant;
	}

	public void execute(final Context context) {
		try {
			System.out.printf("start %s\n", name);
			int acc = constant;
			for (final String input : getInputs()) {
				if (!context.isEdgeSet(input)) {
					throw new RuntimeException("Missing input");
				}
				final Object ob = context.getEdgeValue(input);
				
				if (ob instanceof Integer) {
					acc += (Integer)ob;
				}
			}
			
			for (final String output : getOutputs()) {
				context.setEdgeValue(output, (Integer)acc);
			}
			try { Thread.sleep(100L); } catch (Exception e) {}
		} finally {
			System.out.printf("end %s\n", name);
		}
	}
	
	public static AddOperation.Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String name = "";
		private int constant = 0;
		public final Collection<String> outputsx = Lists.newArrayList();
		public final Collection<String> inputsx = Lists.newArrayList();
		
		public AddOperation.Builder setName(final String name) {
			this.name = name;
			return this;
		}
		
		public AddOperation.Builder setConstant(final int constant) {
			this.constant = constant;
			return this;
		}
		
		public AddOperation.Builder addInput(final String name) {
			inputsx.add(name);
			return this;
		}
		
		public AddOperation.Builder addOutput(final String name) {
			outputsx.add(name);
			return this;
		}
		
		public AddOperation newOperation() {
			return new AddOperation(this);
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public Set<String> getOutputs() {
		return outputsx;
	}

	@Override
	public Set<String> getInputs() {
		return Collections.unmodifiableSet(inputsx);
	}
	
	public static TaskFactory<AddOperation> getTaskFactory(final Context context) {
		return new TaskFactory<AddOperation>(){

			@Override
			public Task create(final Executor executor, final AddOperation node) {
				final Task task = new Task(executor);
				task.addWorker(new Runnable(){

					@Override
					public void run() {
						node.execute(context);
					}});
				return task;
			}};
	}
}