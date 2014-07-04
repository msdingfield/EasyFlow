package msdingfield.easyflow.reflect;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Contains state for the evaluation of a Tasks.
 * 
 * @author Matt
 *
 */
public class Context {

	/**
	 * Map which holds edge values.  The edge values are read and written by
	 * the input/output ports of the operations.
	 */
	private final Map<String, Object> edgeValues = Maps.newConcurrentMap();
	private final Map<Object, Object> stateMap = Maps.newConcurrentMap();

	public void setEdgeValue(final String key, final Object value) {
		edgeValues.put(key, value);
	}

	public Object getEdgeValue(final String key) {
		return edgeValues.get(key);
	}

	public boolean isEdgeSet(final String key) {
		return edgeValues.containsKey(key);
	}

	public void setStateValue(final Object key, final Object value) {
		stateMap.put(key, value);
	}

	public Object getStateValue(final Object key) {
		return stateMap.get(key);
	}

	public boolean isStateVariableSet(final Object key) {
		return stateMap.containsKey(key);
	}

	@Override
	public String toString() {
		return "Context [edgeValues=" + edgeValues + ", stateMap=" + stateMap
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((edgeValues == null) ? 0 : edgeValues.hashCode());
		result = prime * result
				+ ((stateMap == null) ? 0 : stateMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Context other = (Context) obj;
		if (edgeValues == null) {
			if (other.edgeValues != null) {
				return false;
			}
		} else if (!edgeValues.equals(other.edgeValues)) {
			return false;
		}
		if (stateMap == null) {
			if (other.stateMap != null) {
				return false;
			}
		} else if (!stateMap.equals(other.stateMap)) {
			return false;
		}
		return true;
	}

}
