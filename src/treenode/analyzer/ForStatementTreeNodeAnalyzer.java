package treenode.analyzer;

import java.io.File;
import java.util.Enumeration;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import treenode.TreeNodeAnalyzer;

public class ForStatementTreeNodeAnalyzer extends TreeNodeAnalyzer {

	public ForStatementTreeNodeAnalyzer(Node rootNode, Node currentNode, CompilationUnit cUnit, String classPath,
			String className, File file, IProject project) {
		super(rootNode, currentNode, cUnit, classPath, className, file, project);
	}
	
	protected String getName(Node currentNode) {
		String cleanForStateString = null;
		String cleanForInit = null;
		String cleanForIncrDecr = null;
		String forStateString = currentNode.getValue().toString();
		cleanForStateString = cleanStringUp(forStateString);

		Enumeration<?> e = currentNode.children();
		while (e.hasMoreElements()) {
			Node forStatementInnerNode = (Node) e.nextElement();

			if (forStatementInnerNode.getLabel().toString().equals("FOR_INIT")) {
				String forInit = forStatementInnerNode.getValue().toString();
				cleanForInit = cleanStringUp(forInit);
			}
			if (forStatementInnerNode.getLabel().toString().equals("FOR_INCR")
					|| forStatementInnerNode.getLabel().toString().equals("FOR_DECR")) {
				String forIncrDecr = forStatementInnerNode.getValue().toString().replace(" ", "");
				cleanForIncrDecr = cleanStringUp(forIncrDecr);
			}
		}

		String forStatementDeclaration = new String(
				"for(" + cleanForInit + "; " + cleanForStateString + "; " + cleanForIncrDecr + ")");
		return forStatementDeclaration;
	}

	private String cleanStringUp(String s) {
		String s1 = s.replace("(", "");
		String s2 = s1.replace(")", "");
		String s3 = s2.replace(";", "");
		return s3;
	}
}
