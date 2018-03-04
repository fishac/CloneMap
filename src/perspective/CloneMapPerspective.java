package perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import view.CloneMapViewNewVer;
import view.CloneMapViewOldVer;

public class CloneMapPerspective implements IPerspectiveFactory {
	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		IFolderLayout bottomL = layout.createFolder("bottomL", IPageLayout.BOTTOM, 0.5f, editorArea);
		bottomL.addView(CloneMapViewOldVer.VIEW_ID);
		
		layout.addView(CloneMapViewNewVer.VIEW_ID, IPageLayout.RIGHT, 0.5f, "bottomL");
		layout.getViewLayout(CloneMapViewOldVer.VIEW_ID).setCloseable(false);
		layout.getViewLayout(CloneMapViewNewVer.VIEW_ID).setCloseable(false);
	}
}