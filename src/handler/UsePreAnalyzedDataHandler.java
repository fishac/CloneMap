package handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.PreAnalyzedDataAnalyzer;

public class UsePreAnalyzedDataHandler {

	@Execute

	public void execute(EPartService partService) {
		//System.out.println("Analyzing previously gathered data...");
		PreAnalyzedDataAnalyzer pada = new PreAnalyzedDataAnalyzer();
		pada.setEPartService(partService);
		pada.analyze();
		//System.out.println("...done\n");
	}
}
