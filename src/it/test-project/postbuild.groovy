def log = new File((String) basedir, "build.log").getText()

def specFileLineMatcher = log =~ /(?m)\[INFO\] Creating spec file (.*?\.spec)/
def specfile = specFileLineMatcher[0][1]
//println("found spec file" + specfile)

def spec = new File(specfile).getText()

assert spec.contains("URL: https://git.rz.is/groups/aws")
assert spec.contains("Name: spring-boot-packaging-test-project")
assert spec.contains("Version: 1.0")
assert spec =~ /Release: \d{14}/
assert spec.contains("Summary: Integration test for the plugin.")
assert spec.contains("License: Proprietary")
assert spec.contains("Vendor: Immobilien Scout GmbH")
assert spec.contains("URL: https://git.rz.is/groups/aws")
assert spec.contains("Group: application")
assert spec.contains("Packager: Spring Boot Packaging Maven Plugin")
assert spec.contains("Requires: bash")
assert spec.contains("Requires: java-1.7.0-openjdk")

assert spec.contains("""
%pre
if [ "\$1" -eq 1 ]; then
    useradd --comment "spring-boot-packaging-test-proj service user" --shell /sbin/nologin --system --user-group spring-boot-packaging-test-proj --home-dir /usr/share/spring-boot-packaging-test-proj spring-boot-packaging-test-proj
fi
""")

assert spec.contains("""
%files
%defattr(644,root,root,755)
""")
assert spec.contains("%dir %attr(755,root,root) \"/usr/share/spring-boot-packaging-test-proj\"")
assert spec.contains("/usr/share/spring-boot-packaging-test-proj/spring-boot-packaging-test-project-1.0-SNAPSHOT.jar")
assert spec.contains("//usr/share/spring-boot-packaging-test-proj/start.sh")
assert spec.contains("//etc/awslogs.conf.d/spring-boot-packaging-test-proj.conf")
assert spec.contains("//etc/init/spring-boot-packaging-test-proj.conf")
assert spec.contains("//etc/spring-boot-packaging-test-proj/default-logback.xml")
assert spec.contains("%dir %attr(755,spring-boot-packaging-test-proj,spring-boot-packaging-test-proj) \"/var/log/spring-boot-packaging-test-proj\"")

assert log.contains("""
[INFO] BUILD SUCCESS
""");

