package de.is24.aws.mavenplugin.springbootpackaging;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class TemplateService {

	private Handlebars handlebars = new Handlebars();

	public void render(Map<String, String> model, String templatePath, String outputPath) {
		FileWriter out;
		try {
			out = new FileWriter(outputPath);
		} catch (IOException e) {
			throw new RuntimeException("unable to write to: " + outputPath, e);
		}
		render(model, templatePath, out);
	}

	public String render(Map<String, String> model, String templatePath) {
		StringWriter out = new StringWriter();
		render(model, templatePath, out);

		return out.toString();
	}

	public String render(String key, String value, String templatePath) {
		Map<String, String> model = new HashMap<>();
		model.put(key, value);
		return render(model, templatePath);
	}

	private void render(Map<String, String> model, String templatePath, Writer out) {
		try {
			Template template = handlebars.compile(templatePath);
			template.apply(model, out);
		} catch (IOException e) {
			throw new RuntimeException("Processing failed for template: " + templatePath, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
}
