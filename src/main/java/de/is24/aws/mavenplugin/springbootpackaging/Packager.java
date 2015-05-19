package de.is24.aws.mavenplugin.springbootpackaging;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.apache.maven.shared.utils.StringUtils.isEmpty;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;


@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE)
public class Packager extends AbstractMojo {
  private static final List<String> DEFAULT_REQUIRES = Arrays.asList("java-1.7.0-openjdk");

  @Parameter(defaultValue = "${project.artifactId}")
  private String name;
  @Parameter
  private String version;
  @Parameter
  private String release;
  @Parameter(defaultValue = "755")
  private String defaultDirmode;
  @Parameter(defaultValue = "644")
  private String defaultFilemode;
  @Parameter(defaultValue = "root")
  private String defaultUsername;
  @Parameter(defaultValue = "root")
  private String defaultGroupname;
  @Parameter(defaultValue = "${project.description}")
  private String summary;
  @Parameter(defaultValue = "application")
  private String group;
  @Parameter
  private String vendor;
  @Parameter(defaultValue = "Proprietary")
  private String license;
  @Parameter(defaultValue = "${project.url}")
  private String url;
  @Parameter(defaultValue = "Spring Boot Rpm Maven Plugin")
  private String packager;
  @Parameter
  private List<String> requires;
  @Parameter
  private String preinstallScript;
  @Parameter
  private String postinstallScript;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;
  @Parameter(defaultValue = "${session}", required = true, readonly = true)
  private MavenSession session;
  @Component
  private BuildPluginManager pluginManager;

  private TemplateService templateService = new TemplateService();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String application;
    if (project.getArtifactId().length() > 31) {
      application = project.getArtifactId().substring(0, 31);
      getLog().info("cut application name to 32 chars: " + application);
    } else {
      application = project.getArtifactId();
    }

    String artifact = project.getBuild().getFinalName() + "." + project.getPackaging();

    File generatedSourcesDir = procressSources(application, artifact);
    getLog().info("generated sources files to: " + generatedSourcesDir.getAbsolutePath());

    Plugin plugin = plugin("org.codehaus.mojo", "rpm-maven-plugin", "2.1.2");
    MojoExecutor.ExecutionEnvironment env = executionEnvironment(project, session, pluginManager);

    executeMojo(plugin,
      "attached-rpm",
      configuration(
        element("name", name),
        element("group", group),

        //                                              element("version", version),
        element("release", isEmpty(release) ? getBuildTimestamp() : release),
        element("defaultDirmode", defaultDirmode),
        element("defaultFilemode", defaultFilemode),
        element("defaultUsername", defaultUsername),
        element("defaultGroupname", defaultGroupname),
        element("summary", summary),
        element("group", group),
        element("vendor", vendor),
        element("license", license),
        element("url", url),
        element("packager", packager),
        element("requires", mergeRequires()),
        element("preinstallScriptlet",
          element("script", templateService.render("applicationUser", application, "rpm/preinstall"))),
        element("mappings",
          element("mapping",
            element("directory", "/usr/share/" + application),
            element("filemode", "755")),
          element("mapping",
            element("directory", "/usr/share/" + application),
            element("sources", element("source",
                element("location", "${project.build.directory}/" + artifact)))),
          element("mapping",
            element("directory", "/"),
            element("filemode", "755"),
            element("directoryIncluded", "false"),
            element("sources", element("source",
                element("location", generatedSourcesDir.getAbsolutePath())))),
          element("mapping",
            element("directory", "/var/log/" + application),
            element("username", application),
            element("groupname", application),
            element("filemode", "755")))),
      env);

  }

  private String getBuildTimestamp() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return simpleDateFormat.format(session.getStartTime());
  }

  private MojoExecutor.Element[] mergeRequires() {
    Set<String> mergedRequires = new HashSet<>(DEFAULT_REQUIRES);
    if (requires != null) {
      mergedRequires.addAll(requires);
    }

    Collection<MojoExecutor.Element> elements = Collections2.transform(mergedRequires,
      new Function<String, MojoExecutor.Element>() {
        @Nullable
        @Override
        public MojoExecutor.Element apply(String require) {
          return element("require", require);
        }
      });

    return elements.toArray(new MojoExecutor.Element[] {});
  }

  private File procressSources(String application, String artifact) {
    File dir = new File(project.getBuild().getOutputDirectory() + "/generated-resources");
    dir.mkdir();

    Set<String> sourceFiles = new LinkedHashSet<>();
    sourceFiles.add("etc/application/default-logback.xml");
    sourceFiles.add("etc/awslogs/conf.d/application.conf");
    sourceFiles.add("etc/init/application.conf");
    sourceFiles.add("etc/init.d/application");
    sourceFiles.add("usr/share/application/start.sh");

    Map<String, String> model = new HashMap<>();
    model.put(ResourceProcessor.APPLICATION_KEY, application);
    model.put("description", project.getDescription());
    model.put("artifact", artifact);

    getLog().debug("processing sources files: " + Arrays.toString(sourceFiles.toArray()));
    getLog().debug("applying model: " + model);

    Set<String> files = new ResourceProcessor(model, sourceFiles, dir).generate();

    getLog().debug("generated files: " + Arrays.toString(files.toArray()));

    return dir;
  }

}
