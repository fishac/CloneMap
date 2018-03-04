package treenode;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.distilling.WhenChangesAreExtracted;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.util.CompilationUtils;

public class MethodBodyChange extends WhenChangesAreExtracted {

	@Test
	public void test() {
		String filename = "input\\pkg1\\A.java";
		String methodName = "m1";
		Node methodNode = getMethodNode(filename, methodName);

		StringBuilder output = new StringBuilder();
		methodNode.print(output);
		System.out.println(output);
		methodNode.printLabel();

		/*Enumeration<?> e = rootNode.preorderEnumeration();
		while (e.hasMoreElements()) {
			Node iNode = (Node) e.nextElement();
			System.out.println(iNode);
		}*/
	}

	static MethodBodyChange inst = null;

	public static MethodBodyChange getInst() {
		if (inst == null) {
			inst = new MethodBodyChange();
		}
		return inst;
	}

	public Node getMethodNode(String filename, String methodName) {
		JavaCompilation jc = CompilationUtils.compileJFile(filename);
		Node rootNode = convertMethodBody(methodName, jc);
		return rootNode;
	}

	Node getParent(Node n) {
		if (n == null || (n != null && n.getParent() == null))
			return null;
		Node parentNode = null;
		if (n.getParent() instanceof Node) {
			parentNode = (Node) n.getParent();
		}
		return parentNode;
	}

	class MethodDeclVisitor extends ASTVisitor {
		MethodDeclaration myMethod = null;

		@Override
		public boolean visit(MethodDeclaration node) {
			String name = node.getName().getFullyQualifiedName();
			if (myMethod == null && name.contains("foo")) {
				myMethod = node;
			}
			return super.visit(node);
		}

		public MethodDeclaration getMethod() {
			return myMethod;
		}
	}

	void printPreoder(DefaultMutableTreeNode n) {
		Enumeration<?> itr = n.preorderEnumeration();
		while (itr.hasMoreElements()) {
			Object obj = (Object) itr.nextElement();
			System.out.println(obj);
		}
	}

	CompilationUnit parse(char[] unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}
}
