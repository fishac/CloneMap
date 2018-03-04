/*
 * @(#) MyNode.java
 *
 */
package graph.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class GNode {
	private final String id;
	private final String name;
	private List<GNode> connections;
	private String Path;
	private File file;
	private int cloneGroupNumber;
	IProject project = null;

	public GNode(String id, String name, IProject project) {
		this.id = name;
		this.name = name;
		this.connections = new ArrayList<GNode>();
		this.project = project;
		this.cloneGroupNumber = -1;
	}

	public IProject getIProject() {
		return this.project;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<GNode> getConnectedTo() {
		return connections;
	}

	public void resetConnection() {
		connections.clear();
	}

	public void setPath(String path) {
		this.Path = path;
	}

	public String getPath() {
		return this.Path;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public void setCloneGroupNumber(int groupNumber) {
		this.cloneGroupNumber = groupNumber;
	}

	public int getCloneGroupNumber() {
		return this.cloneGroupNumber;
	}
	
	public String toString() {
		String string = "Name: " + getName() + "\n" + "Path: " + getPath() + "\n" + "CloneGroup #: " + getCloneGroupNumber();
		return string;
	}
}
