package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

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

public class PreAnalyzedDataAnalyzer {
	private EPartService partService;

	public void analyze() {
		IProject oldProject = UtilProjects.getOldProject();
		IProject newProject = UtilProjects.getNewProject();

		String oldProjectName = oldProject.getName().replace("_OldVer", "");
		String newProjectName = newProject.getName().replace("_NewVer", "");
		
		String pathOfData = oldProject.getLocation().toString() + '/' + oldProjectName + "-" + newProjectName
		+ "-ReUsableData.csv";
		//System.out.println("You chose " + pathOfData);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(pathOfData));
			//System.out.println("Opened reader");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//System.out.println("Reading data.");
		readThroughData(reader);
	}

	public void readThroughData(BufferedReader reader) {
		CloneMapViewOldVer cMapOld = null;
		CloneMapViewNewVer cMapNew = null;
		try {
			cMapOld = (CloneMapViewOldVer) partService.findPart(CloneMapViewOldVer.VIEW_ID).getObject();
			cMapNew = (CloneMapViewNewVer) partService.findPart(CloneMapViewNewVer.VIEW_ID).getObject();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// "oldVer/newVer,ClonePath,CloneStartLine,CloneEndLine,CloneLength,MethodName,SuccessfulUpdateOrNoChange\n"

		String lineRead;
		String[] data;
		String oldOrNew;
		String projectName;
		int startLine = -1;
		int endLine = -1;
		int LOC = -1;
		String className;
		String methodName;
		boolean successfulUpdateOrNoChange;

		ArrayList<String> lines = new ArrayList<String>();
		try {
			reader.readLine();
			while ((lineRead = reader.readLine()) != null && !(lineRead.contains(",,,,,"))) {
				lines.add(lineRead);
			}
			for (String line : lines) {
				if (line != null && !line.equals(",,,,")) {
					// OlderOrNewer,ProjectName,CloneStartLine,CloneEndLine,CloneLength,ClassName,MethodName,SuccessfulUpdateOrNoChange
					data = line.split(",");
					oldOrNew = data[0];
					projectName = data[1];
					try {
						startLine = Integer.parseInt(data[2]);
						endLine = Integer.parseInt(data[3]);
						LOC = Integer.parseInt(data[4]);
					} catch (Exception e) {
						// this is okay to happen, expected
					}
					className = data[5];
					methodName = data[6];
					successfulUpdateOrNoChange = Boolean.parseBoolean(data[7]);

					if (!successfulUpdateOrNoChange) {
						for (GNode g : GModelBuilderNewVer.instance().getNodes()) {
							if (g.getIProject().getName().replace("_NewVer", "").equals(projectName)) {
								if ((g instanceof GClassNode && g.getName().equals(className))) {
									UtilNode.changeNodeColorToRed(g, cMapNew.graphNodeList);
								} else if (g instanceof GMethodNode && g.getName().equals(methodName)) {
									UtilNode.changeNodeColorToRed(g, cMapNew.graphNodeList);
									turnLeftOutSubMethodNodesRed(g, cMapNew.graphNodeList);
								}
							}
						}
					} else {
						if (oldOrNew.equals("oldVer")) {
							for (GNode g : GModelBuilderOldVer.instance().getNodes()) {
								if (g.getIProject().getName().replace("_OldVer", "").equals(projectName)) {
									if ((g instanceof GClassNode && g.getName().equals(className))
											|| (g instanceof GMethodNode && g.getName().equals(methodName))
											|| (g instanceof GSubMethodNode
													&& ((GSubMethodNode) g).getClassName().equals(className))
													&& ((GSubMethodNode) g).getStartLine() <= endLine
													&& ((GSubMethodNode) g).getEndLine() >= startLine) {
										UtilNode.changeNodeColorToGray(g, cMapOld.graphNodeList);
									}
								}
							}
						} else {
							for (GNode g : GModelBuilderNewVer.instance().getNodes()) {
								if (g.getIProject().getName().replace("_NewVer", "").equals(projectName)) {
									if ((g instanceof GClassNode && g.getName().equals(className))
											|| (g instanceof GMethodNode && g.getName().equals(methodName))
											|| (g instanceof GSubMethodNode
													&& ((GSubMethodNode) g).getClassName().equals(className))
													&& ((GSubMethodNode) g).getStartLine() <= endLine
													&& ((GSubMethodNode) g).getEndLine() >= startLine) {
										UtilNode.changeNodeColorToGray(g, cMapNew.graphNodeList);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setEPartService(EPartService partService) {
		this.partService = partService;
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
}
