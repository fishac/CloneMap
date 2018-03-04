/*
 * @(#) NodeModelContentProvider.java
 *
 */
package graph.builder;

import java.util.ArrayList;
import java.util.List;

import graph.model.GConnection;
import graph.model.GNode;

public class GModelBuilderNewVer {
	private static List<GConnection>	connections		= new ArrayList<GConnection>();
	private static List<GNode>			nodes			= new ArrayList<GNode>();
	static GModelBuilderNewVer				singleton		= null;

	public static List<GNode> getNodes() {
		return nodes;
	}

	public List<GConnection> getConnections() {
		return connections;
	}

	public String getConnectionLabel(String srcId, String dstId) {
		for (GConnection iCon : connections) {
			if (iCon.getSource().getId().equals(srcId) && //
					iCon.getDestination().getId().equals(dstId)) {
				return iCon.getLabel();
			}
		}
		return "";
	}

	public void reset() {
		nodes.clear();
	}
	public static GModelBuilderNewVer instance() {
		if (singleton == null) {
			singleton = new GModelBuilderNewVer();
		}
		return singleton;
	}
	public void setNodes(List<GNode> graph) {
		reset();
		nodes = graph;
	}
	
	public void setConnections(List<GConnection> graph) {
		connections = graph;
	}
}
