package treenode.analyzer;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import treenode.TreeNodeAnalyzer;

public class MethodInvocationTreeNodeAnalyzer extends TreeNodeAnalyzer {

	public MethodInvocationTreeNodeAnalyzer(Node rootNode, Node currentNode, CompilationUnit cUnit, String classPath,
			String className, File file, IProject project) {
		super(rootNode, currentNode, cUnit, classPath, className, file, project);
	}

	protected String getName(Node currentNode) {
		String name = new String(currentNode.getValue());
		return name;
	}
}