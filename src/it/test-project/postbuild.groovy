def log = new File((String) basedir, "build.log").getText()

def specFileLineMatcher = log =~ /(?m)\[INFO\] Creating spec file (.*?\.spec)/
def specfile = specFileLineMatcher[0][1]
//println("found spec file" + specfile)

def spec = new File(specfile).getText()

assert spec.contains("Name: spring-boot-rpm-test-project")
assert spec.contains("Version: 1.0")
assert spec =~ /Release: \d{14}/
assert spec.contains("Summary: Integration test for the plugin.")
assert spec.contains("License: Proprietary")
assert spec.contains("Vendor: Immobilien Scout GmbH")
assert spec.contains("URL: https://github.com/ImmobilienScout24/spring-boot-rpm-maven-plugin")
assert spec.contains("Group: application")
assert spec.contains("Packager: Spring Boot Rpm Maven Plugin")
assert spec.contains("Requires: bash")
assert spec.contains("Requires: java-1.7.0-openjdk")

assert spec.contains("""
%pre
if [ "\$1" -eq 1 ]; then
    useradd --comment "spring-boot-rpm-test-project service user" --shell /sbin/nologin --system --user-group --home-dir /usr/share/spring-boot-rpm-test-project spring-boot-rpm-test-project
fi
""")

assert spec.contains("""
%files
%defattr(644,root,root,755)
""")
assert spec.contains("%dir %attr(755,root,root) \"/usr/share/spring-boot-rpm-test-project\"")
assert spec.contains("/usr/share/spring-boot-rpm-test-project/spring-boot-rpm-test-project-1.0-SNAPSHOT.jar")
assert spec.contains("%attr(755,root,root)  \"//usr/share/spring-boot-rpm-test-project/start.sh")
assert spec.contains("/etc/awslogs.conf.d/spring-boot-rpm-test-project.conf")
assert spec.contains("/etc/init/spring-boot-rpm-test-project.conf")
assert spec.contains("%attr(755,root,root)  \"//etc/init.d/spring-boot-rpm-test-project")
assert spec.contains("/etc/spring-boot-rpm-test-project/default-logback.xml")
assert spec.contains("%dir %attr(755,spring-boot-rpm-test-project,spring-boot-rpm-test-project) \"/var/log/spring-boot-rpm-test-project\"")

assert log.contains("""
[INFO] BUILD SUCCESS
""");

