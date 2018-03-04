package treenode.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import graph.model.node.GSubMethodNode;
import treenode.TreeNodeAnalyzer;

public class SwitchStatementTreeNodeAnalyzer extends TreeNodeAnalyzer {

	public SwitchStatementTreeNodeAnalyzer(Node rootNode, Node currentNode, CompilationUnit cUnit, String classPath,
			String className, File file, IProject project) {
		super(rootNode, currentNode, cUnit, classPath, className, file, project);
	}

	protected String getName(Node currentNode) {
		String name = new String("switch (" + currentNode.getValue() + ")");
		return name;
	}

	protected void visitChildren(Node currentNode, GSubMethodNode rootGNode) {
		Enumeration<?> e = currentNode.children();
		ArrayList<String> caseList = new ArrayList<String>();
		while (e.hasMoreElements()) {
			Node childNode = (Node) e.nextElement();

			if (childNode.getLabel().toString().equals("SWITCH_CASE")) {
				if (childNode.getValue().toString().equals("default")) {
					caseList.add("default:\n");
				} else {
					caseList.add("case " + childNode.getValue().toString() + ":\n");
				}
			} else if (childNode.getLabel().toString().equals("BREAK_STATEMENT")) {
				caseList.add("break;");
				SwitchCaseTreeNodeAnalyzer swCase = new SwitchCaseTreeNodeAnalyzer(currentNode, childNode, caseList,
						this.cUnit, this.classPath, this.className, this.file, project);
				swCase.setSubMSourceNode(rootGNode);
				swCase.analyze();
				caseList.clear();
			} else {
				caseList.add(childNode.getValue().toString() + "\n");
			}
		}

	}
}
