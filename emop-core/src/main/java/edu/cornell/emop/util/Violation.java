package edu.cornell.emop.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Violation {

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
     * Parses the string representation of a violation into a Violation object.
     * This is an example of the string representaiton of a violation:
     * "2 Specification StringTokenizer_HasMoreElements has been violated on line edu.cornell.D.d(D.java:11). Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/StringTokenizer_HasMoreElements.html"
     * The third word is always the violated specification. The ninth word is always a description of the location
     * where the violation occurred. This is parsed into class name and line number. The class
     * information is reformatted to include only the class path with the extension (i.e. without the method
     * information) and uses a backslash (/) instead of a period (.) to match class paths in JGit, which is useful for
     * VMS. In the above example, "edu.cornell.D.d(D.java:11)." has the class name "edu/Cornell/D.java".
     *
     * @param violation Violation line to parse
     * @return Triple of violated specification, class, and line number
     */
    public static Violation parseViolation(String violation) {
        String[] parsedViolation = violation.split(" ");
        String specification = parsedViolation[2];

        String[] classNameAndLineNum = parsedViolation[8].split(":");
        String[] classLocationAndNameExt = classNameAndLineNum[0].split("\\(");
        String[] classLocations = classLocationAndNameExt[0].split("\\.");
        classLocations[classLocations.length - 1] = ""; // remove function name
        classLocations[classLocations.length - 2] = classLocationAndNameExt[1]; // add extension to class name
        String classResult = String.join("/", classLocations);
        String className = classResult.substring(0, classResult.length() - 1);;

        int lineNum = Integer.parseInt(classNameAndLineNum[1].substring(0, classNameAndLineNum[1].indexOf(")")));

        return new Violation(specification, className, lineNum);
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
        return specification.equals(violation.getSpecification())
                && className.equals(violation.getClassName())
                && lineNum == violation.getLineNum();
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
