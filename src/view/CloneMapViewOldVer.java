/*
 * @(#) View.java
 *
 */
package view;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import analysis.ProjectAnalyzer;
import analysis.SelectedClassAnalyzer;
import analysis.SelectedMethodAnalyzer;
import analysis.SelectedPackageAnalyzer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GMethodNode;
import graph.model.node.GPackageNode;
import graph.model.node.GSubMethodNode;
import graph.provider.GLabelProvider;
import graph.provider.GNodeContentProvider;
import util.UtilFile;

public class CloneMapViewOldVer {
	public final static String VIEW_ID = "clonemap.partdescriptor.clonemapviewoldver";
	final static String POPUP_MENU_ID = "clonemap.popupmenu.gobackold";
	private static GraphViewer gViewer;
	private static int layout = 0;
	public String selectedNodeName = null;
	public List<?> graphNodeList = null;
	private Composite parent = null;
	public Menu mPopupMenu = null;
	private MenuItem menuItemGoBack = null;

	private void addPoupMenu() {
		mPopupMenu = new Menu(gViewer.getControl());
		gViewer.getControl().setMenu(mPopupMenu);

		menuItemGoBack = new MenuItem(mPopupMenu, SWT.CASCADE);
		menuItemGoBack.setText("Go Back (Old Graph)");
		addSelectionListenerMenuItemGoBack();
	}

	private void addSelectionListenerMenuItemGoBack() {
		SelectionListener menuItemListenerGoBack = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("\nGoing back...");
				int currentNode = SelectedNodes.nodeClickLogOld.size() - 1;
				if (currentNode < 1) {
					System.out.println("Going back to base graph.");
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					GModelBuilderOldVer.instance().reset();
					for (IProject project : projects) {
						if (project.getName().contains("_OldVer") && project.isOpen()) {
							new ProjectAnalyzer().analyze(project);
							break;
						}
					}
					SelectedNodes.nodeClickLogOld.clear();
					gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
				} else if (currentNode >= 1) {
					int previousNode = currentNode - 1;
					GNode previousNodeClicked = SelectedNodes.nodeClickLogOld.get(previousNode);
					System.out.println(previousNodeClicked.getName());
					GModelBuilderOldVer.instance().reset();
					if (previousNodeClicked instanceof GPackageNode) {
						setDataAnalyzeGraphPackage((GPackageNode) previousNodeClicked);
					} else if (previousNodeClicked instanceof GClassNode) {
						setDataAnalyzeGraphClass((GClassNode) previousNodeClicked);
					} else if (previousNodeClicked instanceof GMethodNode) {
						setDataAnalyzeGraphMethod((GMethodNode) previousNodeClicked);
					}
					gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
					SelectedNodes.nodeClickLogOld.remove(currentNode);
				}
				System.out.println("...done.\n");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		};

		menuItemGoBack.addSelectionListener(menuItemListenerGoBack);
	}

	public Composite getParent() {
		return this.parent;
	}

	@SuppressWarnings("static-access")
	public GraphViewer getGraphViewer() {
		return this.gViewer;
	}

	public List<?> getGraphNodeList() {
		graphNodeList = getGraphViewer().getGraphControl().getNodes();
		return graphNodeList;
	}

	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService) {
		this.parent = parent;
		gViewer = new GraphViewer(parent, SWT.BORDER);
		gViewer.setContentProvider(new GNodeContentProvider());
		gViewer.setLabelProvider(new GLabelProvider());
		gViewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		gViewer.applyLayout();
		addPoupMenu();
		this.graphNodeList = getGraphNodeList();
		Graph graph = gViewer.getGraphControl();
		menuService.registerContextMenu(graph, POPUP_MENU_ID);
		graph.addMouseListener(new MouseAdapter() {
			@SuppressWarnings({ "static-access", "unchecked" })
			public void mouseDoubleClick(MouseEvent e) {
				ISelection selection = gViewer.getSelection();
				Object selected = ((IStructuredSelection) selection).getFirstElement();
				// Check which node is selected by the double click
				if (selected instanceof GSubMethodNode) {
					GSubMethodNode selectedGSMNode = (GSubMethodNode) selected;
					try {
						UtilFile.openFileInEditor(selectedGSMNode.getFile(), selectedGSMNode.getStartOffset(),
								(selectedGSMNode.getEndOffset() - selectedGSMNode.getStartOffset()) + 1);
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}
				} else if (selected instanceof GMethodNode) {
					GMethodNode selectedGMNode = (GMethodNode) selected;

					selectedNodeName = ((GMethodNode) selected).getName();

					SelectedNodes.nodeClickLogOld.add((GMethodNode) selected);

					GModelBuilderOldVer.instance().reset();
					((GMethodNode) selected).resetConnection();

					setDataAnalyzeGraphMethod(selectedGMNode);

					gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
					graphNodeList = getGraphViewer().getGraphControl().getNodes();

					try {
						UtilFile.openFileInEditor(selectedGMNode.getFile(), selectedGMNode.getStartOffset(),
								(selectedGMNode.getEndOffset() - selectedGMNode.getStartOffset()) + 1);
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}

				} else if (selected instanceof GClassNode) {
					GClassNode selectedGCNode = (GClassNode) selected;

					SelectedNodes.nodeClickLogOld.add((GClassNode) selected);
					GModelBuilderOldVer.instance().reset();
					((GClassNode) selected).resetConnection();

					setDataAnalyzeGraphClass(selectedGCNode);
					gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
					graphNodeList = getGraphViewer().getGraphControl().getNodes();

					try {
						UtilFile.openFileInEditor(selectedGCNode.getFile(), 0, 0);
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}

				} else if (selected instanceof GPackageNode) {
					selectedNodeName = ((GPackageNode) selected).getName();
					SelectedNodes.nodeClickLogOld.add((GPackageNode) selected);
					GModelBuilderOldVer.instance().reset();
					((GPackageNode) selected).resetConnection();

					setDataAnalyzeGraphPackage((GPackageNode) selected);
					gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
					graphNodeList = getGraphViewer().getGraphControl().getNodes();
				}
			}
		});
	};

	private void setDataAnalyzeGraphMethod(GMethodNode selected) {
		SelectedMethodAnalyzer analyzerSelectedMethod = new SelectedMethodAnalyzer(selected);
		analyzerSelectedMethod.analyze(selected.getIProject());
	}

	private void setDataAnalyzeGraphClass(GClassNode selected) {
		SelectedClassAnalyzer analyzerSelectedClass = new SelectedClassAnalyzer(selected);
		analyzerSelectedClass.analyze(selected.getIProject());
	}

	private void setDataAnalyzeGraphPackage(GPackageNode selected) {
		SelectedPackageAnalyzer analyzerSelectedPackage = new SelectedPackageAnalyzer(selected);
		analyzerSelectedPackage.analyze(selected.getIProject());
	}

	@SuppressWarnings("static-access")
	public void update() {
		gViewer.setInput(GModelBuilderOldVer.instance().getNodes());
		if (layout % 2 == 0)
			gViewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		else
			gViewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		layout++;
	}

	@SuppressWarnings("static-access")
	@Focus
	public void setFocus() {
		this.gViewer.getGraphControl().setFocus();
	}
}
