package handler;

import java.text.ParseException;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.CloneDataAnalyzer;

public class FindCloneHandler {

	@Execute

	public void execute(EPartService partService) throws ParseException {
		//System.out.println("\nFinding Clones...");
		CloneDataAnalyzer.partService = partService;
		CloneDataAnalyzer.analyzeCloneData();
		
		//clear hashmaps to no conflict with previous data
		//data will be regathered on every click of findclones
		//System.out.println("...done\n");
	}
}
