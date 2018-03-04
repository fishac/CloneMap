/*
 * @(#) ASTZestHandler.java
 *
 * Copyright 2015-2018 The Software Analysis Laboratory
 * Computer Science, The University of Nebraska at Omaha
 * 6001 Dodge Street, Omaha, NE 68182.
 */
package handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.PlatformUI;

import graph.builder.GModelBuilderOldVer;
import graph.model.GNode;
import util.UtilProjects;

public class ASTZestHandler {
	@SuppressWarnings("static-access")
	@Execute
	public void execute(EPartService partService) {
		if(getCurrentPerspectiveName().equals("CloneMap Perspective")) {
			//System.out.println("Showing Clone Graph...");
			UtilProjects.analyzeProjects(partService);
			//System.out.println("...Done.");
		}
	}

	public String getCurrentPerspectiveName() {
		String label = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getLabel();
		return label;
	}
}