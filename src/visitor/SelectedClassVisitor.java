package visitor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GConnection;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GMethodNode;
import graph.model.node.GSubMethodNode;

public class SelectedClassVisitor extends ASTVisitor {

	GClassNode originNode = null;
	Map<String, GNode> nodeMap = new HashMap<String, GNode>();

	private CompilationUnit cUnit = null;
	private String path = null;
	private File file = null;
	private IProject project;

	public SelectedClassVisitor(String classPath, CompilationUnit cUnit, String className, File file,
			IProject project) {
		this.path = classPath;
		this.cUnit = cUnit;
		this.file = file;
		this.project = project;
	}

	public boolean visit(MethodDeclaration methDecl) {

		addSrcNodeInstanceList();

		addConnection(originNode, methDecl);

		return true;
	}

	@SuppressWarnings("static-access")
	void addConnection(GNode originNode, ASTNode astNode) {
		String nodeName = getName(astNode);
		int startPos = astNode.getStartPosition();
		// Add a node
		GNode dstGNode = createGNode(astNode, nodeName);
		dstGNode.setPath(this.path);
		dstGNode.setFile(this.file);

		String className = this.path.substring(this.path.lastIndexOf('/')+1);
		className = className.replace(".java", "");
		dstGNode.setPath(this.path);
		((GMethodNode) dstGNode).setClassName(className);
		((GMethodNode) dstGNode).setPackageName(this.originNode.getPackageName());
		((GMethodNode) dstGNode).setStartLine(this.cUnit.getLineNumber(astNode.getStartPosition()));
		((GMethodNode) dstGNode).setEndLine(this.cUnit.getLineNumber(astNode.getStartPosition() + astNode.getLength()));
		((GMethodNode) dstGNode).setStartOffset(astNode.getStartPosition());
		((GMethodNode) dstGNode).setEndOffset(astNode.getStartPosition() + astNode.getLength() - 1);

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
		if (astNode instanceof MethodDeclaration) {
			return ((MethodDeclaration) astNode).getName().getFullyQualifiedName();
		} else {
			return ((TypeDeclaration) astNode).getName().getFullyQualifiedName();
		}
	}

	GNode createGNode(ASTNode astNode, String nodeName) {
		String dstGNodeId = nodeName + astNode.getStartPosition();
		if (astNode instanceof MethodDeclaration) {
			return new GMethodNode(dstGNodeId, nodeName, project);
		} else {
			return new GSubMethodNode(dstGNodeId, nodeName, project);
		}
	}

	public void setSourceNode(GClassNode originNode) {
		this.originNode = originNode;
	}

	@SuppressWarnings("static-access")
	void addSrcNodeInstanceList() {
		// if the list does not contain the origin node, set the origin node to
		// position 0
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
}
