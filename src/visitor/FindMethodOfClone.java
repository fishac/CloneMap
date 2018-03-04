package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import analysis.CloneDataAnalyzer;
import clone.CloneData;

public class FindMethodOfClone extends ASTVisitor {
	CompilationUnit cUnit = null;
	CloneData oldCloneData = null;
	CloneData newCloneData = null;
	String methodToMatch = null;

	public FindMethodOfClone(CompilationUnit cUnit, CloneData oldCloneData, CloneData newCloneData,
			String methodToMatch) {
		this.cUnit = cUnit;
		this.oldCloneData = oldCloneData;
		this.newCloneData = newCloneData;
		this.methodToMatch = methodToMatch;
	}

	public boolean visit(MethodDeclaration methodDecl) {
		int currentMethodStart = cUnit.getLineNumber(methodDecl.getStartPosition());
		int currentMethodEnd = cUnit.getLineNumber(methodDecl.getLength() + methodDecl.getStartPosition());
		if (CloneDataAnalyzer.currentlySearchingForOldMethod) {
			if (currentMethodStart <= oldCloneData.startline && currentMethodEnd >= oldCloneData.endline) {
				CloneDataAnalyzer.foundMethodName = methodDecl.getName().getFullyQualifiedName();
				return false;
			}
		} else {
			if (methodDecl.getName().getFullyQualifiedName().equals(methodToMatch)) {
				if (currentMethodStart <= newCloneData.startline && currentMethodEnd >= newCloneData.endline) {
					int updateBuffer = (int) Math.ceil(((double) oldCloneData.LOC) * .1);

					if (newCloneData.LOC >= (oldCloneData.LOC - updateBuffer)
							&& newCloneData.LOC <= (oldCloneData.LOC + updateBuffer)) {
						CloneDataAnalyzer.foundNewMethodSuccessfully = true;
						return false;
					}
				}
			}
		}
		return super.visit(methodDecl);
	}
}
