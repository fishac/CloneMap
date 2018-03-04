package visitor;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import graph.model.GNode;
import graph.model.node.GMethodNode;
import treenode.*;
import treenode.analyzer.AssignmentStatementTreeNodeAnalyzer;
import treenode.analyzer.BreakStatementTreeNodeAnalyzer;
import treenode.analyzer.CatchClauseTreeNodeAnalyzer;
import treenode.analyzer.ConstructorInvocationTreeNodeAnalyzer;
import treenode.analyzer.ContinueStatemenTreeNodeAnalyzer;
import treenode.analyzer.DoStatementTreeNodeAnalyzer;
import treenode.analyzer.EnhancedForEachStatementTreeNodeAnalyzer;
import treenode.analyzer.FinallyTreeNodeAnalyzer;
import treenode.analyzer.ForStatementTreeNodeAnalyzer;
import treenode.analyzer.IfStatementTreeNodeAnalyzer;
import treenode.analyzer.MethodInvocationTreeNodeAnalyzer;
import treenode.analyzer.PostfixExpressionTreeNodeAnalyzer;
import treenode.analyzer.ReturnStatementTreeNodeAnalyzer;
import treenode.analyzer.TryStatementTreeNodeAnalyzer;
import treenode.analyzer.VariableDeclarationStatementTreeNodeAnalyzer;
import treenode.analyzer.WhileStatementTreeNodeAnalyzer;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class SelectedMethodVisitor extends ASTVisitor {
	Map<String, GNode> nodeMap = new HashMap<String, GNode>();
	private CompilationUnit cUnit = null;
	private String classPath = null;
	private String className = null;
	private File file = null;
	private IProject project;
	private GMethodNode selectedGNode;

	public SelectedMethodVisitor(GMethodNode selectedGNode, String classPath, CompilationUnit cUnit, String className,
			File file, IProject project) {
		this.selectedGNode = selectedGNode;
		this.classPath = classPath;
		this.cUnit = cUnit;
		this.className = className;
		this.file = file;
		this.project = project;
	}

	public boolean visit(MethodDeclaration methodDecl) {
		if (methodDecl.getName().getFullyQualifiedName().equals(this.selectedGNode.getName())) {
			Node methodTreeNode = MethodBodyChange.getInst().getMethodNode(classPath, this.selectedGNode.getName());
			SourceCodeEntity srcCodeEnt = methodTreeNode.getEntity();
			if (this.selectedGNode.getStartOffset() == srcCodeEnt.getStartPosition()
					&& this.selectedGNode.getEndOffset() == srcCodeEnt.getEndPosition()) {
				System.out.println("Visiting internals.");
				visitInternals(this.selectedGNode.getName(), methodDecl, srcCodeEnt, methodTreeNode);
				return false;
			}
		}
		return super.visit(methodDecl);
	}

	public static ASTNode getOuterClass(ASTNode node) {
		do {
			// Keep searching if this node is not null and
			// it is not a type declaration node and
			// it is a child of compilation unit node.
			node = node.getParent();
		} while (node != null && node.getNodeType() != ASTNode.TYPE_DECLARATION && //
				((AbstractTypeDeclaration) node).isPackageMemberTypeDeclaration());
		return node;
	}

	void visitInternals(String selectedMethod, MethodDeclaration methodDecl, SourceCodeEntity srcCodeEnt,
			Node methodTreeNode) {
		System.out.println("Begin visiting internals.");
		Enumeration<?> e = methodTreeNode.children();
		while (e.hasMoreElements()) {
			Node childNode = (Node) e.nextElement();
			if (childNode.getLabel().toString().equals("ASSIGNMENT")) {
				AssignmentStatementTreeNodeAnalyzer assignState = new AssignmentStatementTreeNodeAnalyzer(
						methodTreeNode, childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				assignState.setSourceNode(this.selectedGNode);
				assignState.analyze();
			} else if (childNode.getLabel().toString().equals("BREAK_STATEMENT")) {
				BreakStatementTreeNodeAnalyzer breakState = new BreakStatementTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				breakState.setSourceNode(this.selectedGNode);
				breakState.analyze();
			} else if (childNode.getLabel().toString().equals("CATCH_CLAUSE")) {
				CatchClauseTreeNodeAnalyzer catchCl = new CatchClauseTreeNodeAnalyzer(methodTreeNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				catchCl.setSourceNode(this.selectedGNode);
				catchCl.analyze();
			} else if (childNode.getLabel().toString().equals("CONSTRUCTOR_INVOCATION")) {
				ConstructorInvocationTreeNodeAnalyzer conInv = new ConstructorInvocationTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				conInv.setSourceNode(this.selectedGNode);
				conInv.analyze();
			} else if (childNode.getLabel().toString().equals("CONTINUE_STATEMENT")) {
				ContinueStatemenTreeNodeAnalyzer contState = new ContinueStatemenTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				contState.setSourceNode(this.selectedGNode);
				contState.analyze();
			} else if (childNode.getLabel().toString().equals("DO_STATEMENT")) {
				DoStatementTreeNodeAnalyzer doState = new DoStatementTreeNodeAnalyzer(methodTreeNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				doState.setSourceNode(this.selectedGNode);
				doState.analyze();
			} else if (childNode.getLabel().toString().equals("FINALLY")) {
				FinallyTreeNodeAnalyzer finAnalyzer = new FinallyTreeNodeAnalyzer(methodTreeNode, childNode, this.cUnit,
						this.classPath, this.className, this.file, this.project);
				finAnalyzer.analyze();
			} else if (childNode.getLabel().toString().equals("FOREACH_STATEMENT")) {
				EnhancedForEachStatementTreeNodeAnalyzer enhForState = new EnhancedForEachStatementTreeNodeAnalyzer(
						methodTreeNode, childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				enhForState.setSourceNode(this.selectedGNode);
				enhForState.analyze();
			} else if (childNode.getLabel().toString().equals("FOR_STATEMENT")) {
				ForStatementTreeNodeAnalyzer forState = new ForStatementTreeNodeAnalyzer(methodTreeNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				forState.setSourceNode(this.selectedGNode);
				forState.analyze();
			} else if (childNode.getLabel().toString().equals("IF_STATEMENT")) {
				IfStatementTreeNodeAnalyzer ifState = new IfStatementTreeNodeAnalyzer(methodTreeNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				ifState.setSourceNode(this.selectedGNode);
				ifState.analyze();
			} else if (childNode.getLabel().toString().equals("METHOD_INVOCATION")) {
				MethodInvocationTreeNodeAnalyzer methodInv = new MethodInvocationTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				methodInv.setSourceNode(this.selectedGNode);
				methodInv.analyze();
			} else if (childNode.getLabel().toString().equals("POSTFIX_EXPRESSION")) {
				PostfixExpressionTreeNodeAnalyzer postfixExpr = new PostfixExpressionTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				postfixExpr.setSourceNode(this.selectedGNode);
				postfixExpr.analyze();
			} else if (childNode.getLabel().toString().equals("RETURN_STATEMENT")) {
				ReturnStatementTreeNodeAnalyzer returnState = new ReturnStatementTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				returnState.setSourceNode(this.selectedGNode);
				returnState.analyze();
			} else if (childNode.getLabel().toString().equals("TRY_STATEMENT")) {
				TryStatementTreeNodeAnalyzer tryState = new TryStatementTreeNodeAnalyzer(methodTreeNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				tryState.setSourceNode(this.selectedGNode);
				tryState.analyze();
			} else if (childNode.getLabel().toString().equals("VARIABLE_DECLARATION_STATEMENT")) {
				VariableDeclarationStatementTreeNodeAnalyzer varDecl = new VariableDeclarationStatementTreeNodeAnalyzer(
						methodTreeNode, childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				varDecl.setSourceNode(this.selectedGNode);
				varDecl.analyze();
			} else if (childNode.getLabel().toString().equals("WHILE_STATEMENT")) {
				WhileStatementTreeNodeAnalyzer whileState = new WhileStatementTreeNodeAnalyzer(methodTreeNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				whileState.setSourceNode(this.selectedGNode);
				whileState.analyze();
			}
		}
	}
}
