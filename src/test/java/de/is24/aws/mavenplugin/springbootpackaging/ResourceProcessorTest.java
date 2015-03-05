package de.is24.aws.mavenplugin.springbootpackaging;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResourceProcessorTest {

	private static final String APPLICATION_NAME = "resourceprocessortest";
	private final Map<String, String> model = new HashMap<>();
	private File targetDir;
	private ResourceProcessor resourceProcessor;

	@Before
	public void setUp() {
		model.put(ResourceProcessor.APPLICATION_KEY, APPLICATION_NAME);

		targetDir = new File(System.getProperty("java.io.tmpdir") + "/springbootrpm-test");
		targetDir.mkdir();

		Set<String> sourceFiles = new HashSet<>();
		sourceFiles.add("etc/application/default-logback.xml");
		sourceFiles.add("etc/awslogs.conf.d/application.conf");
		sourceFiles.add("etc/init/application.conf");
		sourceFiles.add("usr/share/application/start.sh");

		resourceProcessor = new ResourceProcessor(model, sourceFiles, targetDir);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(targetDir);
	}

	@Test
	public void generatesLogbackXml() throws IOException {
		final String filename = "/etc/" + APPLICATION_NAME + "/default-logback.xml";

		Set<String> files = resourceProcessor.generate();

		String generated = Iterables.find(files, new Predicate<String>() {
			@Override
			public boolean apply(String s) {
				return s.endsWith(filename);
			}
		});

		String expected = IOUtils.toString(getClass().getResourceAsStream(filename));
		assertThat(readFileToString(new File(generated)), equalTo(expected));
	}

	@Test
	public void generatesCloudWatchLogsConfig() throws IOException {
		final String filename = "/etc/awslogs.conf.d/" + APPLICATION_NAME + ".conf";

		Set<String> files = resourceProcessor.generate();

		String generated = Iterables.find(files, new Predicate<String>() {
			@Override
			public boolean apply(String s) {
				return s.endsWith(filename);
			}
		});

		String expected = IOUtils.toString(getClass().getResourceAsStream(filename));
		assertThat(readFileToString(new File(generated)), equalTo(expected));
	}

	@Test
	public void generatesUpstartJob() throws IOException {
		final String filename = "/etc/init/" + APPLICATION_NAME + ".conf";
		model.put("description", "This is the description text");

		Set<String> files = resourceProcessor.generate();

		String generated = Iterables.find(files, new Predicate<String>() {
			@Override
			public boolean apply(String s) {
				return s.endsWith(filename);
			}
		});

		String expected = IOUtils.toString(getClass().getResourceAsStream(filename));
		assertThat(readFileToString(new File(generated)), equalTo(expected));
	}

	@Test
	public void generatesStartScript() throws IOException {
		final String filename = "/usr/share/" + APPLICATION_NAME + "/start.sh";
		model.put("description", "This is the description text");
		model.put("artifact", "resourceprocessortest.jar");

		Set<String> files = resourceProcessor.generate();

		String generated = Iterables.find(files, new Predicate<String>() {
			@Override
			public boolean apply(String s) {
				return s.endsWith(filename);
			}
		});

		String expected = IOUtils.toString(getClass().getResourceAsStream(filename));
		assertThat(readFileToString(new File(generated)), equalTo(expected));
	}
}