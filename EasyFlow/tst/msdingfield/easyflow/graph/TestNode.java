package msdingfield.easyflow.graph;

import java.util.Set;

import com.google.common.collect.Sets;

public class TestNode implements GraphNode {

	private String name = null;
	private Set<String> outputs = Sets.newHashSet();
	private Set<String> inputs = Sets.newHashSet();
	
	public TestNode(final String name) {
		this.name = name;
	}
	
	@Override
	public Set<String> getOutputs() {
		return Sets.newHashSet(outputs);
	}

	@Override
	public Set<String> getInputs() {
		return Sets.newHashSet(inputs);
	}
	
	public TestNode withInput(final String ... names) {
		for (final String name : names) {
			inputs.add(name);
		}
		return this;
	}
	
	public TestNode withOutput(final String ... names) {
		for (final String name : names) {
			outputs.add(name);
		}
		return this;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestNode other = (TestNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}