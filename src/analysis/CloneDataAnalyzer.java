/*
 * @(#) ASTAnalyzer.java
 *
 * Copyright 2015-2018 The Software Analysis Laboratory
 * Computer Science, The University of Nebraska at Omaha
 * 6001 Dodge Street, Omaha, NE 68182.
 */
package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import clone.CloneData;
import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GMethodNode;
import graph.model.node.GSubMethodNode;
import util.UtilNode;
import util.UtilProjects;
import view.CloneMapViewNewVer;
import view.CloneMapViewOldVer;

public class CloneDataAnalyzer {
	public static EPartService partService;
	public static String foundMethodName = null;
	private static StringBuilder sb = null;
	public static boolean foundNewMethodSuccessfully;
	public static boolean currentlySearchingForOldMethod;

	static LinkedList<CloneData> linkedlistOld = new LinkedList<CloneData>();
	static LinkedList<CloneData> linkedlistNew = new LinkedList<CloneData>();

	public static void analyzeCloneData() {
		// System.out.println("start");
		sb = new StringBuilder();
		IProject oldProject = UtilProjects.getOldProject();
		IProject newProject = UtilProjects.getNewProject();

		String oldProjectName = oldProject.getName().replace("_OldVer", "");
		String newProjectName = newProject.getName().replace("_NewVer", "");
		
		try {
			String oldCSVName = oldProject.getLocation().toString() + '/' + oldProjectName + ".csv";
			String newCSVName = newProject.getLocation().toString() + '/' + newProjectName + ".csv";

			String pathForOutputData = oldProject.getLocation().toString() + '/' + oldProjectName + "-" + newProjectName
					+ "-ReUsableData.csv";

			readInCloneData(oldProjectName, newProjectName, oldCSVName, newCSVName);
			markClones();
			findMissingClones(oldProject, newProject);
			saveAnalyzedData(pathForOutputData);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	private static void readInCloneData(String oldProjectName, String newProjectName, String csvFileOld,
			String csvFileNew) {
		// System.out.println("Reading in old clone data.");
		readInOldCloneData(oldProjectName, csvFileOld);
		// System.out.println("Reading in new clone data.");
		readInNewCloneData(newProjectName, csvFileNew);
	}

	private static void readInOldCloneData(String oldProjectName, String csvFileOld) {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(csvFileOld));

			String line = "";

			int PathIndex = 1;
			int StartLineIndex = 2;
			int EndLineIndex = 3;
			int LOCIndex = 4;
			reader.readLine();
			while ((line = reader.readLine()) != null && !(line.contains(",,,"))) {
				String[] cloneDataInfo = line.split(",");

				String path = cloneDataInfo[PathIndex];
				int startLine = Integer.parseInt(cloneDataInfo[StartLineIndex]);
				int endLine = Integer.parseInt(cloneDataInfo[EndLineIndex]);
				int LOC = Integer.parseInt(cloneDataInfo[LOCIndex]);

				CloneData clone = new CloneData(path, startLine, endLine, LOC);

				linkedlistOld.add(clone);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void readInNewCloneData(String newProjectName, String csvFileNew) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(csvFileNew));
			String line = "";

			int PathIndex = 1;
			int StartLineIndex = 2;
			int EndLineIndex = 3;
			int LOCIndex = 4;

			reader.readLine();
			while ((line = reader.readLine()) != null && !(line.contains(",,,"))) {
				String[] cloneDataInfo = line.split(",");

				String path = cloneDataInfo[PathIndex];
				int startLine = Integer.parseInt(cloneDataInfo[StartLineIndex]);
				int endLine = Integer.parseInt(cloneDataInfo[EndLineIndex]);
				int LOC = Integer.parseInt(cloneDataInfo[LOCIndex]);

				CloneData clone = new CloneData(path, startLine, endLine, LOC);

				linkedlistNew.add(clone);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void markClones() {
		// System.out.println("Marking old clones.");
		if (!GModelBuilderOldVer.instance().getNodes().isEmpty())
			markClonesOld();
		// System.out.println("Marking new clones.");
		if (!GModelBuilderNewVer.instance().getNodes().isEmpty())
			markClonesNew();
	}

	private static void markClonesOld() {
		MPart oldViewPart = partService.findPart(CloneMapViewOldVer.VIEW_ID);
		CloneMapViewOldVer oldViewObject = (CloneMapViewOldVer) oldViewPart.getObject();

		for (CloneData data : linkedlistOld) {
			int startline = data.startline;
			int endline = data.endline;
			String pathUpToPackage = data.path.substring(0, data.path.lastIndexOf('\\'));
			// ex: C:/Workspace/proj/src/pkg
			int posOfSlashBeforePackage = pathUpToPackage.lastIndexOf('\\');
			String PackageNameAndClassName = data.path.substring(posOfSlashBeforePackage + 1);
			// ex: pkg/A.java
			for (GNode g : GModelBuilderOldVer.instance().getNodes()) {
				if (g.getPath().replace("/", "\\").trim().contains(PackageNameAndClassName)) {
					if (g instanceof GMethodNode) {
						if (((GMethodNode) g).getStartLine() <= startline
								&& ((GMethodNode) g).getEndLine() >= endline) {
							UtilNode.changeNodeColorToGray(g, oldViewObject.graphNodeList);
						}
					} else if (g instanceof GSubMethodNode) {
						if (((GSubMethodNode) g).getStartLine() >= startline
								&& ((GSubMethodNode) g).getEndLine() <= endline) {
							UtilNode.changeNodeColorToGray(g, oldViewObject.graphNodeList);
						}
					} else if (g instanceof GClassNode) {
						// if there is a clone within the class
						UtilNode.changeNodeColorToGray(g, oldViewObject.graphNodeList);
					}
				}
			}
		}
	}

	private static void markClonesNew() {
		MPart newViewPart = partService.findPart(CloneMapViewNewVer.VIEW_ID);
		CloneMapViewNewVer newViewObject = (CloneMapViewNewVer) newViewPart.getObject();

		for (CloneData data : linkedlistNew) {
			int startline = data.startline;
			int endline = data.endline;
			String pathUpToPackage = data.path.substring(0, data.path.lastIndexOf('\\'));
			int posOfSlashBeforePackage = pathUpToPackage.lastIndexOf('\\');
			String PackageNameAndClassName = data.path.substring(posOfSlashBeforePackage + 1);
			// ex: pkg/A.java

			for (GNode g : GModelBuilderNewVer.instance().getNodes()) {
				if (g.getPath().replace("/", "\\").trim().contains(PackageNameAndClassName)) {
					if (g instanceof GMethodNode) {
						if (((GMethodNode) g).getStartLine() <= startline
								&& ((GMethodNode) g).getEndLine() >= endline) {
							UtilNode.changeNodeColorToGray(g, newViewObject.graphNodeList);
						}
					} else if (g instanceof GSubMethodNode) {
						if (((GSubMethodNode) g).getStartLine() >= startline
								&& ((GSubMethodNode) g).getEndLine() <= endline) {
							UtilNode.changeNodeColorToGray(g, newViewObject.graphNodeList);
						}
					} else if (g instanceof GClassNode) {
						// if there is a clone within the class
						UtilNode.changeNodeColorToGray(g, newViewObject.graphNodeList);
					}
				}
			}
		}
	}

	// determines if a node has not been updated along with the rest of the
	// group, if yes turns it red
	private static void findMissingClones(IProject oldProject, IProject newProject) {
		// System.out.println("finding left out clones from old revision to
		// new");

		try {
			sb.append(
					"OlderOrNewer,ProjectName,CloneStartLine,CloneEndLine,CloneLength,ClassName,MethodName,SuccessfulUpdateOrNoChange\n");

			// int i = 1;
			for (CloneData cloneOld : linkedlistOld) {
				// System.out.println(i++);
				String classNameOldClone = cloneOld.path.substring(cloneOld.path.lastIndexOf('\\') + 1);
				classNameOldClone = classNameOldClone.replace(".java", "");

				verifyCloneUpdated(cloneOld, classNameOldClone, oldProject, newProject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void verifyCloneUpdated(CloneData oldClone, String classNameOldClone, IProject oldProject,
			IProject newProject) {
		foundMethodName = null;
		currentlySearchingForOldMethod = true;
		FindMethodFromCloneData fmfcd = new FindMethodFromCloneData();
		fmfcd.setDataToMatch(classNameOldClone, oldClone, null, null);
		fmfcd.analyze(oldProject);// look for method represented by clone data,
									// set "foundMethodName" = to it

		String oldMethodToMatch = foundMethodName;

		sb.append("oldVer" + "," + oldProject.getName().replace("_OldVer", "") + "," + oldClone.startline + ","
				+ oldClone.endline + "," + (oldClone.endline - oldClone.startline + 1) + "," + classNameOldClone + ","
				+ foundMethodName + ",true\n");

		currentlySearchingForOldMethod = false;
		if (oldMethodToMatch != null) {
			// System.out.println("Found method " + foundMethodName + " in
			// oldProject, looking in new project.");

			for (CloneData newClone : linkedlistNew) {
				String classNameNewClone = newClone.path.substring(newClone.path.lastIndexOf('\\') + 1);
				classNameNewClone = classNameNewClone.replace(".java", "");
				if (classNameNewClone.equals(classNameOldClone)) {
					foundNewMethodSuccessfully = false;
					fmfcd.setDataToMatch(classNameOldClone, oldClone, newClone, oldMethodToMatch);
					fmfcd.analyze(newProject);

					if (foundNewMethodSuccessfully) {
						// System.out.println(oldMethodToMatch + " was
						// updated.");

						sb.append(
								"newVer" + "," + newProject.getName().replace("_NewVer", "") + "," + newClone.startline
										+ "," + newClone.endline + "," + (newClone.endline - newClone.startline + 1)
										+ "," + classNameOldClone + "," + foundMethodName + ",true\n");
						linkedlistNew.remove(newClone);
						return;
					}
				}
			}
		}
		sb.append("newVer" + "," + newProject.getName().replace("_NewVer", "") + ",N/A,N/A,N/A," + classNameOldClone
				+ "," + oldMethodToMatch + ",false\n");
		turnLeftOutNodesRed(classNameOldClone, oldMethodToMatch, oldClone);
	}

	private static void turnLeftOutNodesRed(String classNameToMatch, String oldMethodName, CloneData c) {
		MPart newViewPart = partService.findPart(CloneMapViewNewVer.VIEW_ID);
		CloneMapViewNewVer newView = (CloneMapViewNewVer) newViewPart.getObject();

		turnLeftOutContainingClassNodeRed(classNameToMatch, newView.graphNodeList);
		turnLeftOutMethodRed(oldMethodName, classNameToMatch, newView.graphNodeList);
	}

	private static void turnLeftOutMethodRed(String oldMethodName, String classNameToMatch, List<?> graphNodeList) {
		if (!GModelBuilderNewVer.instance().getNodes().isEmpty()) {
			for (GNode g : GModelBuilderNewVer.instance().getNodes()) {
				if (g instanceof GMethodNode) {
					if (g.getName().equals(oldMethodName)) {
						if (((GMethodNode) g).getClassName().replace(".java", "").equals(classNameToMatch)) {

							UtilNode.changeNodeColorToRed(g, graphNodeList);
							turnLeftOutSubMethodNodesRed(g, graphNodeList);
							break;
						}
					}
				}
			}
		}
	}

	private static void turnLeftOutContainingClassNodeRed(String classNameToMatch, List<?> graphNodeList) {
		GNode classNode = getClassNode(classNameToMatch);
		if (classNode != null) {
			UtilNode.changeNodeColorToRed(classNode, graphNodeList);
		}
	}

	private static void turnLeftOutSubMethodNodesRed(GNode g, List<?> graphNodeList) {
		if (!GModelBuilderNewVer.instance().getNodes().isEmpty()) {
			for (GNode h : GModelBuilderNewVer.instance().getNodes()) {
				if (h instanceof GSubMethodNode) {
					if (((GSubMethodNode) h).getRootGNode().getName().equals(g.getName())) {

						UtilNode.changeNodeColorToRed(h, graphNodeList);

						if (((GSubMethodNode) h).hasChildren()) {
							turnLeftOutSubMethodNodesRed(h, graphNodeList);
						}
					}
				}
			}
		}
	}

	private static GNode getClassNode(String className) {
		for (GNode g : GModelBuilderNewVer.instance().getNodes()) {
			if (g instanceof GClassNode) {
				if (g.getName().equals(className)) {
					return g;
				}
			}
		}
		return null;
	}

	private static void saveAnalyzedData(String pathForData) {
		try {
			PrintWriter pw = new PrintWriter(new File(pathForData));
			pw.write(sb.toString());
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}