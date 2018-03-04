/*
 * @(#) GClassNode.java
 *
 */
package graph.model.node;

import org.eclipse.core.resources.IProject;

import graph.model.GNode;

public class GClassNode extends GNode {
	private String packageName;
	public GClassNode(String id, String name, IProject project) {
		super(id, name, project);
	}
	public void setPackageName(String pName) {
		this.packageName = pName;
	}

	public String getPackageName() {
		return this.packageName;
	}
}
