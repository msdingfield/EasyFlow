package msdingfield.easyflow.reflect;

import java.util.Map;

import com.google.common.collect.Maps;

/** Contains state for the evaluation of a Tasks. */
public class Context {
	private Map<Object, Object> ports = Maps.newConcurrentMap();
	
	public Context setInput(final Map<Object, Object> input) {
		for (Map.Entry<Object, Object> e : input.entrySet()) {
			putPortValue(e.getKey(), e.getValue());
		}
		return this;
	}
	
	public void putPortValue(final Object key, final Object value) {
		ports.put(key, value);
	}
	
	public Object getPortValue(final Object key) {
		return ports.get(key);
	}
	
	public boolean isPortSet(final Object key) {
		return ports.containsKey(key);
	}
	
	public String toString() {
		return ports.toString();
	}
}
