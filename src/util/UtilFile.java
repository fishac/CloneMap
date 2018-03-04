package util; 

import java.io.File;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import view.CloneMapViewOldVer;

public class UtilFile {
	public static void openFileInEditor(File file, int startPos, int length) throws PartInitException {
		File fileToOpen = file;
		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			try {
				ITextEditor editor = (ITextEditor) IDE.openEditorOnFileStore(page, fileStore);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CloneMapViewOldVer.VIEW_ID);

				// 1st parameter startPos: starting position of selected block, which is offset instead of line number.
				// 2nd parameter 0: the length of selected block, counted as offset instead of line number.
				editor.selectAndReveal(startPos, length);
			} catch (PartInitException e) {
				// Put your exception handler here if you wish to
			}
		} else {
			// Do something if the file does not exist
		}
	}
}