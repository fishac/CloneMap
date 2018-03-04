package visitor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GConnection;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GPackageNode;

public class SelectedPackageVisitor extends ASTVisitor {
	
	
	GPackageNode originNode = null;
	Map<String, GNode>	nodeMap	= new HashMap<String, GNode>();
	public IPackageFragment iPackage = null;
	private File file;
	private String path;
	private IProject project;
	
	public SelectedPackageVisitor(IProject project) {
		this.project = project;
	}
	
	public boolean visit(TypeDeclaration typeDecl) {
		addSrcNodeInstanceList();
		System.out.println(typeDecl.getName().getFullyQualifiedName());
		addConnection(originNode, typeDecl);
		return true;
	}
	
	@SuppressWarnings("static-access")
	void addConnection(GPackageNode originNode, ASTNode astNode) {
		System.out.println("Adding connection");
		String nodeName = getName(astNode);
		int startPos = astNode.getStartPosition();
		// Add a node
		GNode dstGNode = createGNode(astNode, nodeName);
		dstGNode.setPath(this.path);
		dstGNode.setFile(this.file);
		((GClassNode)dstGNode).setPackageName(this.originNode.getName());
			
		// Add a connection
		String conId = originNode.getId() + dstGNode.getId();
		String conLabel = "offset: " + astNode.getStartPosition();
		GConnection con = new GConnection(conId, conLabel, originNode, dstGNode);
		if (project.getName().contains("_OldVer")) {
			GModelBuilderOldVer.instance().getNodes().add(dstGNode);
			GModelBuilderOldVer.instance().getConnections().add(con);
		} else {
			GModelBuilderNewVer.instance().getNodes().add(dstGNode);
			GModelBuilderNewVer.instance().getConnections().add(con);
		}
		originNode.getConnectedTo().add(dstGNode);
		// Update map
		nodeMap.put(nodeName + ":" + startPos, dstGNode);
		
	}
	String getName(ASTNode astNode) {
		if (astNode instanceof TypeDeclaration) {
			return ((TypeDeclaration) astNode).getName().getFullyQualifiedName();
		} else {
			return ((MethodDeclaration) astNode).getName().getFullyQualifiedName();
		}
	}
	
	GNode createGNode(ASTNode astNode, String nodeName) {
		String dstGNodeId = nodeName + astNode.getStartPosition();
		if (astNode instanceof TypeDeclaration) {
			return new GClassNode(dstGNodeId, nodeName, project);
		} else {
			return new GPackageNode(dstGNodeId, nodeName, project);
		}
	}
	
	public void setSourceNode(GPackageNode pkg) {
		this.originNode = pkg;
	}
	@SuppressWarnings("static-access")
	void addSrcNodeInstanceList() {
		//if the list does not contain the origin node, set the origin node to position 0
		if (project.getName().contains("_OldVer")) {
			if (!(GModelBuilderOldVer.instance().getNodes().contains(this.originNode))) {
				GModelBuilderOldVer.instance().getNodes().add(this.originNode);
			}
		} else {
			if (!(GModelBuilderNewVer.instance().getNodes().contains(this.originNode))) {
				GModelBuilderNewVer.instance().getNodes().add(this.originNode);
			}
		}
	}
	
	public void setFileAndPath(File file, String path) {
		this.file = file;
		this.path = path;
	}
}
