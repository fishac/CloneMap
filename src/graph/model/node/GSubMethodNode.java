package graph.model.node;

import org.eclipse.core.resources.IProject;

import graph.model.GNode;

public class GSubMethodNode extends GNode {
	private String className;
	private int startLine;
	private int endLine;
	private int startOffset;
	private int endOffset;
	private GNode rootNode;
	private String packageName;
	private boolean hasChildren;

	public GSubMethodNode(String id, String name, IProject project) {
		super(id, name, project);
	}
	public void setPackageName(String pName) {
		this.packageName = pName;
	}

	public String getPackageName() {
		return this.packageName;
	}
	public void setRootGNode(GNode root) {
		this.rootNode = root;
	}

	public GNode getRootGNode() {
		return this.rootNode;
	}
	
	public void setClassName(String cName) {
		this.className = cName;
	}

	public String getClassName() {
		return this.className;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getStartLine() {
		return this.startLine;
	}

	public int getEndLine() {
		return this.endLine;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getStartOffset() {
		return this.startOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public int getEndOffset() {
		return this.endOffset;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean has) {
		this.hasChildren = has;
	}
	@Override
	public String toString() {
		String string = super.toString();
		string += "\nStart Line: " + getStartLine();
		string += "\nEnd Line: " + getEndLine();
		return string;
	}
}
