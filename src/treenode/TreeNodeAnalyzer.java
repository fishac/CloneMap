package treenode;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;

import analysis.SelectedMethodAnalyzer;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GConnection;
import graph.model.GNode;
import graph.model.node.GMethodNode;
import graph.model.node.GSubMethodNode;
import treenode.analyzer.AssignmentStatementTreeNodeAnalyzer;
import treenode.analyzer.BodyTreeNodeAnalyzer;
import treenode.analyzer.BreakStatementTreeNodeAnalyzer;
import treenode.analyzer.CatchClauseTreeNodeAnalyzer;
import treenode.analyzer.CatchClausesTreeNodeAnalyzer;
import treenode.analyzer.ConstructorInvocationTreeNodeAnalyzer;
import treenode.analyzer.ContinueStatemenTreeNodeAnalyzer;
import treenode.analyzer.DoStatementTreeNodeAnalyzer;
import treenode.analyzer.ElseStatementTreeNodeAnalyzer;
import treenode.analyzer.EnhancedForEachStatementTreeNodeAnalyzer;
import treenode.analyzer.FinallyTreeNodeAnalyzer;
import treenode.analyzer.ForStatementTreeNodeAnalyzer;
import treenode.analyzer.IfStatementTreeNodeAnalyzer;
import treenode.analyzer.MethodInvocationTreeNodeAnalyzer;
import treenode.analyzer.PostfixExpressionTreeNodeAnalyzer;
import treenode.analyzer.ReturnStatementTreeNodeAnalyzer;
import treenode.analyzer.ThenStatementTreeNodeAnalyzer;
import treenode.analyzer.TryStatementTreeNodeAnalyzer;
import treenode.analyzer.VariableDeclarationStatementTreeNodeAnalyzer;
import treenode.analyzer.WhileStatementTreeNodeAnalyzer;

public abstract class TreeNodeAnalyzer {

	GMethodNode originMNode = null;
	GSubMethodNode originSubMNode = null;
	Map<String, GNode> nodeMap = new HashMap<String, GNode>();
	Node rootNode = null;
	Node currentNode = null;
	protected CompilationUnit cUnit = null;
	protected String classPath = null;
	protected String className = null;
	protected File file = null;
	protected IProject project;

	public TreeNodeAnalyzer(Node rootNode, Node currentNode, CompilationUnit cUnit, String classPath, String className,
			File file, IProject project) {
		this.rootNode = rootNode;
		this.currentNode = currentNode;
		this.cUnit = cUnit;
		this.classPath = classPath;
		this.className = className;
		this.file = file;
		this.project = project;
	}

	public void analyze() {
		addSrcNodeInstanceList();
		if (originMNode != null)
			addConnection(originMNode, currentNode);
		else if (originSubMNode != null) {
			addConnection(originSubMNode, currentNode);
		}
	}

	@SuppressWarnings("static-access")
	void addConnection(GNode originNode, Node currentNode) {
		if (originNode instanceof GSubMethodNode) {
			((GSubMethodNode) originNode).setHasChildren(true);
		}
		SourceCodeEntity srcCodeEnt = currentNode.getEntity();
		String nodeName = getName(currentNode);
		int startPos = srcCodeEnt.getStartPosition();
		// Add a node
		GSubMethodNode dstGNode = createGNode(currentNode, nodeName);
		setGNodeCodeBlockStartPos(dstGNode, srcCodeEnt);
		setGNodeCodeBlockEndPos(dstGNode, srcCodeEnt);
		dstGNode.setPath(this.classPath);
		dstGNode.setRootGNode(originNode);
		dstGNode.setClassName(this.className);
		dstGNode.setStartOffset(srcCodeEnt.getStartPosition());
		dstGNode.setEndOffset(srcCodeEnt.getEndPosition());
		dstGNode.setFile(this.file);

		// Add a connection
		String conId = originNode.getId() + dstGNode.getId();
		String conLabel = "offset: " + startPos;
		GConnection con = new GConnection(conId, conLabel, originNode, dstGNode);
		originNode.getConnectedTo().add(dstGNode);
		if (project.getName().contains("_OldVer")) {
			GModelBuilderOldVer.instance().getNodes().add(dstGNode);
			GModelBuilderOldVer.instance().getConnections().add(con);
		} else {
			GModelBuilderNewVer.instance().getNodes().add(dstGNode);
			GModelBuilderNewVer.instance().getConnections().add(con);
		}

		// Update map
		nodeMap.put(nodeName + ":" + startPos, dstGNode);
		visitChildren(currentNode, dstGNode);
	}

	GSubMethodNode createGNode(Node currentNode, String nodeName) {
		String dstGNodeId = nodeName + currentNode.hashCode();
		return new GSubMethodNode(dstGNodeId, nodeName, this.project);
	}

	protected abstract String getName(Node currentNode);

	private void setGNodeCodeBlockStartPos(GSubMethodNode gNode, SourceCodeEntity srcCodeEnt) {
		int startPos = this.cUnit.getLineNumber(srcCodeEnt.getStartPosition());
		gNode.setStartLine(startPos);
	}

	private void setGNodeCodeBlockEndPos(GSubMethodNode gNode, SourceCodeEntity srcCodeEnt) {
		int endPos = this.cUnit.getLineNumber(srcCodeEnt.getEndPosition());
		gNode.setEndLine(endPos);
	}

	public void setSourceNode(GMethodNode methDecl) {
		this.originMNode = methDecl;
	}

	public void setSubMSourceNode(GSubMethodNode subMNode) {
		this.originSubMNode = subMNode;
	}

	@SuppressWarnings("static-access")
	void addSrcNodeInstanceList() {
		// if the list does not contain the origin node, add it to the instance
		// list
		if (project.getName().contains("_OldVer")) {
			if (this.originMNode != null) {
				if (!(GModelBuilderOldVer.instance().getNodes().contains(this.originMNode))) {
					GModelBuilderOldVer.instance().getNodes().add(this.originMNode);
				}
			} else if (this.originSubMNode != null) {
				if (!(GModelBuilderOldVer.instance().getNodes().contains(this.originSubMNode))) {
					GModelBuilderOldVer.instance().getNodes().add(this.originSubMNode);
				}
			}
		} else {
			if (this.originMNode != null) {
				if (!(GModelBuilderNewVer.instance().getNodes().contains(this.originMNode))) {
					GModelBuilderNewVer.instance().getNodes().add(this.originMNode);
				}
			} else if (this.originSubMNode != null) {
				if (!(GModelBuilderNewVer.instance().getNodes().contains(this.originSubMNode))) {
					GModelBuilderNewVer.instance().getNodes().add(this.originSubMNode);
				}
			}
		}

	}

	protected void visitChildren(Node currentNode, GSubMethodNode gNode) {
		Enumeration<?> e = currentNode.children();
		while (e.hasMoreElements()) {
			Node childNode = (Node) e.nextElement();
			if (childNode.getLabel().toString().equals("ASSIGNMENT")) {
				AssignmentStatementTreeNodeAnalyzer assignState = new AssignmentStatementTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				assignState.setSubMSourceNode(gNode);
				assignState.analyze();
			} else if (childNode.getLabel().toString().equals("BREAK_STATEMENT")) {
				BreakStatementTreeNodeAnalyzer breakState = new BreakStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				breakState.setSubMSourceNode(gNode);
				breakState.analyze();
			} else if (childNode.getLabel().toString().equals("BODY")) {
				BodyTreeNodeAnalyzer bodyNode = new BodyTreeNodeAnalyzer(currentNode, childNode, this.cUnit,
						this.classPath, this.className, this.file, this.project);
				bodyNode.setSubMSourceNode(gNode);
				bodyNode.analyze();
			} else if (childNode.getLabel().toString().equals("CATCH_CLAUSE")) {
				CatchClauseTreeNodeAnalyzer catchCl = new CatchClauseTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				catchCl.setSubMSourceNode(gNode);
				catchCl.analyze();
			} else if (childNode.getLabel().toString().equals("CATCH_CLAUSES")) {
				CatchClausesTreeNodeAnalyzer catchCls = new CatchClausesTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				catchCls.setSubMSourceNode(gNode);
				catchCls.analyze();
			} else if (childNode.getLabel().toString().equals("CONSTRUCTOR_INVOCATION")) {
				ConstructorInvocationTreeNodeAnalyzer conInv = new ConstructorInvocationTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				conInv.setSubMSourceNode(gNode);
				conInv.analyze();
			} else if (childNode.getLabel().toString().equals("CONTINUE_STATEMENT")) {
				ContinueStatemenTreeNodeAnalyzer contState = new ContinueStatemenTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				contState.setSubMSourceNode(gNode);
				contState.analyze();
			} else if (childNode.getLabel().toString().equals("DO_STATEMENT")) {
				DoStatementTreeNodeAnalyzer doState = new DoStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				doState.setSubMSourceNode(gNode);
				doState.analyze();
			} else if (childNode.getLabel().toString().equals("ELSE_STATEMENT")) {
				ElseStatementTreeNodeAnalyzer elseState = new ElseStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				elseState.setSubMSourceNode(gNode);
				elseState.analyze();
			} else if (childNode.getLabel().toString().equals("FINALLY")) {
				FinallyTreeNodeAnalyzer finAnalyzer = new FinallyTreeNodeAnalyzer(currentNode, childNode, this.cUnit,
						this.classPath, this.className, this.file, this.project);
				finAnalyzer.analyze();
			} else if (childNode.getLabel().toString().equals("FOREACH_STATEMENT")) {
				EnhancedForEachStatementTreeNodeAnalyzer enhForState = new EnhancedForEachStatementTreeNodeAnalyzer(
						currentNode, childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				enhForState.setSubMSourceNode(gNode);
				enhForState.analyze();
			} else if (childNode.getLabel().toString().equals("FOR_STATEMENT")) {
				ForStatementTreeNodeAnalyzer forState = new ForStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				forState.setSubMSourceNode(gNode);
				forState.analyze();
			} else if (childNode.getLabel().toString().equals("IF_STATEMENT")) {
				IfStatementTreeNodeAnalyzer ifState = new IfStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				ifState.setSubMSourceNode(gNode);
				ifState.analyze();
			} else if (childNode.getLabel().toString().equals("METHOD_INVOCATION")) {
				MethodInvocationTreeNodeAnalyzer methodInv = new MethodInvocationTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				methodInv.setSubMSourceNode(gNode);
				methodInv.analyze();
			} else if (childNode.getLabel().toString().equals("POSTFIX_EXPRESSION")) {
				PostfixExpressionTreeNodeAnalyzer postfixExpr = new PostfixExpressionTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				postfixExpr.setSubMSourceNode(gNode);
				postfixExpr.analyze();
			} else if (childNode.getLabel().toString().equals("RETURN_STATEMENT")) {
				ReturnStatementTreeNodeAnalyzer returnState = new ReturnStatementTreeNodeAnalyzer(currentNode,
						childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				returnState.setSubMSourceNode(gNode);
				returnState.analyze();
			} else if (childNode.getLabel().toString().equals("THEN_STATEMENT")) {
				ThenStatementTreeNodeAnalyzer thenNode = new ThenStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				thenNode.setSubMSourceNode(gNode);
				thenNode.analyze();
			} else if (childNode.getLabel().toString().equals("TRY_STATEMENT")) {
				TryStatementTreeNodeAnalyzer tryState = new TryStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				tryState.setSubMSourceNode(gNode);
				tryState.analyze();
			} else if (childNode.getLabel().toString().equals("VARIABLE_DECLARATION_STATEMENT")) {
				VariableDeclarationStatementTreeNodeAnalyzer varDecl = new VariableDeclarationStatementTreeNodeAnalyzer(
						currentNode, childNode, this.cUnit, this.classPath, this.className, this.file, this.project);
				varDecl.setSubMSourceNode(gNode);
				varDecl.analyze();
			} else if (childNode.getLabel().toString().equals("WHILE_STATEMENT")) {
				WhileStatementTreeNodeAnalyzer whileState = new WhileStatementTreeNodeAnalyzer(currentNode, childNode,
						this.cUnit, this.classPath, this.className, this.file, this.project);
				whileState.setSubMSourceNode(gNode);
				whileState.analyze();
			}
		}
	}
}
