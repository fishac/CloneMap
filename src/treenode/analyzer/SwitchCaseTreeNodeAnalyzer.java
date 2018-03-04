package treenode.analyzer;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import treenode.TreeNodeAnalyzer;

public class SwitchCaseTreeNodeAnalyzer extends TreeNodeAnalyzer {

	ArrayList<String> caseList = null;

	public SwitchCaseTreeNodeAnalyzer(Node rootNode, Node currentNode, ArrayList<String> caseList,
			CompilationUnit cUnit, String classPath, String className, File file, IProject project) {
		super(rootNode, currentNode, cUnit, classPath, className, file, project);
		this.caseList = caseList;
	}

	@Override
	protected String getName(Node currentNode) {
		String name = new String("");
		for (int i = 0; i < caseList.size(); i++) {
			name = name + caseList.get(i);
		}
		return name;
	}
}
