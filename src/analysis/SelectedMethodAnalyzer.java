/*
 * @(#) ASTAnalyzer.java
 *
 * Copyright 2015-2018 The Software Analysis Laboratory
 * Computer Science, The University of Nebraska at Omaha
 * 6001 Dodge Street, Omaha, NE 68182.
 */
package analysis;

import java.io.File;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import graph.model.GNode;
import graph.model.node.GMethodNode;
import visitor.SelectedMethodVisitor;

public class SelectedMethodAnalyzer {
	private static final String JAVANATURE = "org.eclipse.jdt.core.javanature";
	private GMethodNode selectedGNode;

	public SelectedMethodAnalyzer(GMethodNode selectedGNode) {
		this.selectedGNode = selectedGNode;
	}

	public void analyze(IProject project) {
		try {
			analyzeJavaProject(project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void analyzeJavaProject(IProject project) throws CoreException, JavaModelException {
		// Check if we have a Java project.
		if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) {
			return;
		}
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment iPackage : packages) {
			// Package fragments include all packages in the classpath.
			// We will only look at the package from the source folder,
			// indicating this root only contains source files.
			// K_BINARY would include also included JARS, e.g. rt.jar.
			if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (iPackage.getCompilationUnits().length < 1) {
					continue;
				}
				if (iPackage.getElementName().equals(this.selectedGNode.getPackageName()))
					analyzeCompilationUnit(iPackage, javaProject, project);
			}
		}
	}

	private void analyzeCompilationUnit(IPackageFragment iPackage, IJavaProject javaProject, IProject project)
			throws JavaModelException {
		for (ICompilationUnit iUnit : iPackage.getCompilationUnits()) {
			CompilationUnit compilationUnit = parse(iUnit);
			String iClassName = compilationUnit.getJavaElement().getElementName();
			String className = iClassName.replaceAll(".java", "");
			if (className.equals(this.selectedGNode.getClassName())) {
				IResource resource = iUnit.getUnderlyingResource();
				String path = null;
				File file = null;
				if (resource.getType() == IResource.FILE) {
					IFile ifile = (IFile) resource;
					path = ifile.getRawLocation().toString();
					URI uri = ifile.getLocationURI();
					if (ifile.isLinked()) {
						uri = ifile.getRawLocationURI();
					}
					try {
						file = EFS.getStore(uri).toLocalFile(0, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				SelectedMethodVisitor v = new SelectedMethodVisitor(selectedGNode, path, compilationUnit, className,
						file, project);
				compilationUnit.accept(v);
				break;
			}
		}
	}

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file. Constant for indicating the AST API that handles JLS8.
	 * This API is capable of handling all constructs in the Java language as
	 * described in the Java Language Specification, Java SE 8 Edition (JLS8) as
	 * specified by JSR337. JLS8 is a superset of all earlier versions of the
	 * Java language, and the JLS8 API can be used to manipulate programs
	 * written in all versions of the Java language up to and including Java SE
	 * 8 (aka JDK 1.8).
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}