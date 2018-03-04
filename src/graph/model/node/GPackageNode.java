/*
 * @(#) GPackageNode.java
 *
 */
package graph.model.node;

import org.eclipse.core.resources.IProject;

import graph.model.GNode;

public class GPackageNode extends GNode {

	public GPackageNode(String id, String name, IProject project) {
		super(id, name, project);
	}
}
