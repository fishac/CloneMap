/*
 * @(#) ZestLabelProvider.java
 *
 */
package graph.provider;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;

import graph.builder.GModelBuilderNewVer;
import graph.builder.GModelBuilderOldVer;
import graph.model.GNode;
import graph.model.node.GClassNode;
import graph.model.node.GMethodNode;
import graph.model.node.GPackageNode;

public class GLabelProvider extends LabelProvider implements IEntityStyleProvider {
	public String getText(Object element) {
		// Create a label for node.
		if (element instanceof GNode) {
			GNode myNode = (GNode) element;
			return myNode.getName();
		}
		// Create a label for connection.
		if (element instanceof EntityConnectionData) {
			EntityConnectionData eCon = (EntityConnectionData) element;
			if (eCon.source instanceof GNode) {
				GNode node = (GNode) eCon.source;
				if (node.getIProject().getName().contains("_OldVer"))
					return GModelBuilderOldVer.instance().getConnectionLabel( //
							((GNode) eCon.source).getId(), //
							((GNode) eCon.dest).getId());
				else
					return GModelBuilderNewVer.instance().getConnectionLabel( //
							((GNode) eCon.source).getId(), //
							((GNode) eCon.dest).getId());
			}
		}
		return "";
	}

	@Override
	public boolean fisheyeNode(Object arg0) {
		return false;
	}

	@Override
	public Color getBackgroundColour(Object o) {
		return getNodeColor(o);
	}

	@Override
	public Color getNodeHighlightColor(Object o) {
		return getNodeColor(o);
	}

	@Override
	public Color getForegroundColour(Object arg0) {
		return ColorConstants.black;
	}

	@Override
	public Color getBorderHighlightColor(Object arg0) {
		return ColorConstants.red;
	}

	private Color getNodeColor(Object o) {
		if (o instanceof GPackageNode) {
			return ColorConstants.lightGreen;
		}
		if (o instanceof GClassNode) {
			return ColorConstants.lightBlue;
		}
		if (o instanceof GMethodNode) {
			return ColorConstants.yellow;
		} else {
			return ColorConstants.orange;
		}
	}

	@Override
	public Color getBorderColor(Object arg0) {
		return null;
	}

	@Override
	public int getBorderWidth(Object arg0) {
		return 0;
	}

	@Override
	public IFigure getTooltip(Object o) {

		if (o instanceof GClassNode) {
			IFigure tooltip = new Figure();
			tooltip.setBorder(new MarginBorder(5, 5, 5, 5));
			FlowLayout layout = new FlowLayout(false);
			layout.setMajorSpacing(3);
			layout.setMinorAlignment(3);
			tooltip.setLayoutManager(new FlowLayout(false));
			tooltip.add(new Label(((GClassNode) o).getName()));
			return tooltip;
		}

		return null;
	}
}
