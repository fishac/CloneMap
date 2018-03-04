package util;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.zest.core.widgets.GraphNode;

import graph.model.GNode;

public class UtilNode {
	public static GraphNode getGraphNode(GNode gNode, List<?> graphNodeList) {
		String nameGNode = gNode.getName();
		GraphNode resultNode = null;
		for (Object object : graphNodeList) {
			GraphNode node = (GraphNode) object;
			String nameGraphNode = ((GNode) node.getData()).getName();
			if (nameGraphNode.equals(nameGNode)) {
				resultNode = node;
				break;
			}
		}
		return resultNode;
	}

	public static void changeNodeColorToGray(GNode g, List<?> graphNodeList) {
		GraphNode correspondingGraphNode = UtilNode.getGraphNode(g, graphNodeList);
		correspondingGraphNode.setBackgroundColor(ColorConstants.lightGray);
		correspondingGraphNode.setHighlightColor(ColorConstants.lightGray);
	}
	
	public static void changeNodeColorToRed(GNode g, List<?> graphNodeList) {
		GraphNode correspondingGraphNode = UtilNode.getGraphNode(g, graphNodeList);
		correspondingGraphNode.setBackgroundColor(ColorConstants.red);
		correspondingGraphNode.setHighlightColor(ColorConstants.red);
	}
}
