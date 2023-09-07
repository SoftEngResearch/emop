package edu.cornell.emop.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Represents the Java object for a violation. */
public class Violation {

    /**
     * Pattern for the string representation of a violation. This is an example of such a string:
     * <pre>
     *   "2 Specification StringTokenizer_HasMoreElements has been violated on line edu.cornell.D.d(D.java:11). Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/StringTokenizer_HasMoreElements.html"
     * </pre>
     * The violated specification (group 1) is <code>StringTokenizer_HasMoreElements</code>.
     * The method (group 2) is <code>edu.cornell.D.d</code>.
     * The line number (group 3) is <code>11</code>.
     */
    public static final Pattern pattern =
            Pattern.compile("(?:\\d+ )?Specification (\\S+) has been violated on line ([^(]+)\\([^:]*:(\\d+)\\).*");

    public static final Pattern specPattern =
            Pattern.compile("(?:\\d+ )?Specification (\\S+) has been violated on line [^(]*\\(Unknown(?: Source)?\\).*");

    private String specification;
    private String className;
    private int lineNum;

    public Violation(String specification, String className, int lineNum) {
        this.specification = specification;
        this.className = className;
        this.lineNum = lineNum;
    }

    public String getSpecification() {
        return specification;
    }

    public String getClassName() {
        return className;
    }

    public int getLineNum() {
        return lineNum;
    }

    /**
     * Analyzes a violations file and returns a set of violations.
     *
     * @param violationsPath The file where violations are located
     * @return A set of violations, each violation is a triple containing the specification, a class, and a line number
     */
    public static Set<Violation> parseViolations(Path violationsPath) {
        try {
            return Files.readAllLines(violationsPath)
                    .stream()
                    .map(Violation::parseViolation)
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return new HashSet<>();
        }
    }

    /**
     * Analyzes a violations file and returns the set of specifications that were violated.
     *
     * @param violationsPath The file where violations are located
     * @return A set of violated specifications
     */
    public static Set<String> parseViolationSpecs(Path violationsPath) {
        return parseViolations(violationsPath).stream().map(Violation::getSpecification).collect(Collectors.toSet());
    }

    /**
     * Parses the string representation of a violation into a Violation object. See the documentation
     * for {@link Violation#pattern} for a description of the string representation.
     *
     * <p>The class information is reformatted to use a backslash (/) instead of a period (.) to match
     * class paths in JGit, e.g., <code>edu/cornell/D.java</code>.
     *
     * @param violation Violation line to parse
     * @return Triple of violated specification, class, and line number
     */
    public static Violation parseViolation(String violation) {
        Matcher matcher = pattern.matcher(violation);

        if (!matcher.matches()) {
            Matcher specMatcher = specPattern.matcher(violation);
            if (specMatcher.matches()) {
                return new Violation(specMatcher.group(1), null, -1);
            } else {
                throw new IllegalArgumentException("Could not parse violation: illegal string representation");
            }
        }

        String method = matcher.group(2);
        String className = method.substring(0, method.lastIndexOf('.')).replace('.', '/') + ".java";

        return new Violation(matcher.group(1), className, Integer.parseInt(matcher.group(3)));
    }

    /**
     * Whether a violation has a known location or not. Violations with unknown locations must have a non-null class
     * name and a valid line number where the violation occurred.
     *
     * @return Whether the violation has a known location where it occurred
     */
    public boolean hasKnownLocation() {
        return this.getClassName() != null && this.getLineNum() >= 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Violation)) {
            return false;
        }
        if (object == this) {
            return true;
        }

        Violation violation = (Violation) object;
        return Objects.equals(specification, violation.specification)
                && Objects.equals(className, violation.className)
                && lineNum == violation.lineNum;
    }

    @Override
    public String toString() {
        return "(" + specification + ", " + className + ", " + lineNum + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(specification, className, lineNum);
    }
}
