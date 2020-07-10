package org.eclipse.lsp4mp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyGraph {
	// If we want to resolve values, it would be easier if we used Property

	// private Map<String /* name of property */, List<Property>> nodes;

	private Map<String /* name of property */, List<String> /* adjacency list */> nodes;

	public PropertyGraph() {
		// this.nodes = new HashMap<String, List<Property>>();
		this.nodes = new HashMap<String, List<String>>();
	}
	
	public void addNode(String property) {
		this.nodes.put(property, new ArrayList<String>());
	}

	public boolean hasNode(String property) {
		return this.nodes.containsKey(property);
	}

	public void addEdge(String from, String to) {
		this.nodes.get(from).add(to);
	}
	
	public boolean isConnected(String from, String to) {
		// TODO check if there exists a path from -> to
		return false;
	}

}