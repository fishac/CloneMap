/*
 * @(#) MethodVisitor.java
 *
 * Copyright 2015-2018 The Software Analysis Laboratory
 * Computer Science, The University of Nebraska at Omaha
 * 6001 Dodge Street, Omaha, NE 68182.
 */
package visitor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GConnection;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GMethodNode;

public class DeclarationVisitor extends ASTVisitor {
	GNode pkgGNode;
	Map<String, GNode> nodeMap = new HashMap<String, GNode>();
	String filepath = null;
	CompilationUnit cUnit = null;
	File file = null;
	IProject project;

	public DeclarationVisitor(GNode pkgNode, CompilationUnit cUnit, String filepath, File file, IProject project) {
		this.pkgGNode = pkgNode;
		this.cUnit = cUnit;
		this.filepath = filepath;
		this.file = file;
		this.project = project;
	}

	/**
	 * A type declaration is the union of a class declaration and an interface
	 * declaration.
	 */
	@Override
	public boolean visit(TypeDeclaration typeDecl) {
		GNode srcGNode = this.pkgGNode;
		addConnection(srcGNode, typeDecl);

		return super.visit(typeDecl);

	}

	@SuppressWarnings("static-access")
	void addConnection(GNode srcGNode, ASTNode astNode) {
		// Set a node name
		String nodeName = getName(astNode);
		int startPos = astNode.getStartPosition();
		// Add a node
		GNode dstGNode = createGNode(astNode, nodeName);
		dstGNode.setPath(filepath);
		dstGNode.setFile(this.file);

		if (astNode instanceof MethodDeclaration) {
			String className = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length() - 5);
			((GMethodNode) dstGNode).setClassName(className);
			((GMethodNode) dstGNode).setStartLine(this.cUnit.getLineNumber(astNode.getStartPosition()));
			((GMethodNode) dstGNode)
					.setEndLine(this.cUnit.getLineNumber(astNode.getStartPosition() + astNode.getLength()));
			((GMethodNode) dstGNode).setPackageName(this.pkgGNode.getName());
			((GMethodNode) dstGNode).setStartOffset(astNode.getStartPosition());
			((GMethodNode) dstGNode).setEndOffset(astNode.getStartPosition() + astNode.getLength() - 1);
		} else if (astNode instanceof TypeDeclaration) {
			((GClassNode) dstGNode).setPackageName(this.pkgGNode.getName());
			
		}
		
		// Add a connection
		String conId = srcGNode.getId() + dstGNode.getId();
		String conLabel = "offset: " + astNode.getStartPosition();
		GConnection con = new GConnection(conId, conLabel, srcGNode, dstGNode);
		
		if (project.getName().contains("_OldVer")) {
			GModelBuilderOldVer.instance().getNodes().add(dstGNode);
			GModelBuilderOldVer.instance().getConnections().add(con);
		} else {
			GModelBuilderNewVer.instance().getNodes().add(dstGNode);
			GModelBuilderNewVer.instance().getConnections().add(con);
		}
		srcGNode.getConnectedTo().add(dstGNode);
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
			return new GClassNode(dstGNodeId, nodeName, this.project);
		} else {
			return new GMethodNode(dstGNodeId, nodeName, this.project);
		}
	}

	@Override
	public boolean visit(MethodDeclaration methodDecl) {
		ASTNode astNodeObj = getOuterClass(methodDecl);
		if (astNodeObj instanceof TypeDeclaration) {
			TypeDeclaration typeNode = (TypeDeclaration) astNodeObj;
			String typeName = typeNode.getName().getFullyQualifiedName();
			int startPos = typeNode.getStartPosition();
			GNode typeGNode = nodeMap.get(typeName + ":" + startPos);
			if (typeGNode == null) {
				throw new RuntimeException();
			} else {
				addConnection(typeGNode, methodDecl);
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
		} while (node != null && node.getNodeType() != ASTNode.TYPE_DECLARATION// && //
				/*((AbstractTypeDeclaration) node).isPackageMemberTypeDeclaration()*/);
		return node;
	}
}
