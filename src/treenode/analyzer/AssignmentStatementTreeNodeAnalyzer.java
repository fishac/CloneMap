package treenode.analyzer;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import graph.model.node.GSubMethodNode;
import treenode.TreeNodeAnalyzer;

public class AssignmentStatementTreeNodeAnalyzer extends TreeNodeAnalyzer {
	
	public AssignmentStatementTreeNodeAnalyzer(Node rootNode, Node currentNode, CompilationUnit cUnit, String classPath,
			String className, File file, IProject project) {
		super(rootNode, currentNode, cUnit, classPath, className, file, project);
	}
	
	@Override
	protected String getName(Node currentNode) {
		String name = new String(currentNode.getValue());
		return name;
	}

	@Override
	protected void visitChildren(Node currentNode, GSubMethodNode gNode) {
	}
}
