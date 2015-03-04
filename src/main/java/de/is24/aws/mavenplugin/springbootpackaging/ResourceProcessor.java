package de.is24.aws.mavenplugin.springbootpackaging;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ResourceProcessor {

	public static final String APPLICATION_KEY = "application";
	private final Set<String> sourceFiles;
	private final File targetDir;
	private final TemplateService templateService = new TemplateService();
	private final Map<String, String> model;

	public ResourceProcessor(Map<String, String> model, Set<String> sourceFiles, File targetDir) {
		this.sourceFiles = sourceFiles;
		this.model = model;
		this.targetDir = targetDir;
	}

	public Set<String> generate() {
		Set<String> outputFiles = new LinkedHashSet<>();
		for (String file : sourceFiles) {
			String outputPath = targetDir.getAbsolutePath() + "/" + file.replaceAll(APPLICATION_KEY, model.get(APPLICATION_KEY));
			File outputDir = new File(outputPath).getParentFile();
			outputDir.mkdirs();
			templateService.render(model, file, outputPath);

			outputFiles.add(outputPath);
		}

		return outputFiles;
	}
}
