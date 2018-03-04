package util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import analysis.ProjectAnalyzer;
import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import view.CloneMapViewNewVer;
import view.CloneMapViewOldVer;

public class UtilProjects {
	private static IProject[] projects;

	public static boolean verifyProjectsOpen() {
		projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int projectsOpen = 0;
		for (IProject project : projects) {
			if (project.exists() && project.isOpen()) {
				projectsOpen++;
			}
		}
		if (projectsOpen != 2) {
			UtilMsg.openWarning("Please open only two comparable projects.");
			return false;
		}
		return true;
	}

	public static void analyzeProjects(EPartService partService) {
		MPart oldView = partService.findPart(CloneMapViewOldVer.VIEW_ID);
		MPart newView = partService.findPart(CloneMapViewNewVer.VIEW_ID);
		if (UtilProjects.verifyProjectsOpen()) {
			projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				if (project.getName().contains("_OldVer") && project.isOpen()) {
					new ProjectAnalyzer().analyze(project);
					((CloneMapViewOldVer) oldView.getObject()).getGraphViewer()
							.setInput(GModelBuilderOldVer.instance().getNodes());

				} else if (project.getName().contains("_NewVer") && project.isOpen()) {
					new ProjectAnalyzer().analyze(project);
					((CloneMapViewNewVer) newView.getObject()).getGraphViewer()
							.setInput(GModelBuilderNewVer.instance().getNodes());

				}
			}
		}
	}
	public static IProject getOldProject() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(IProject project : projects) {
			if (project.getName().contains("_OldVer") && project.isOpen()) {
				return project;
			}
		}
		return null;
	}
	public static IProject getNewProject() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(IProject project : projects) {
			if (project.getName().contains("_NewVer") && project.isOpen()) {
				return project;
			}
		}
		return null;
	}
}
