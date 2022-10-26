package edu.cornell.emop.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Violation {

    private String specification;
    private String classInfo;
    private int lineNum;

    public Violation(String specification, String classInfo, int lineNum) {
        this.specification = specification;
        this.classInfo = classInfo;
        this.lineNum = lineNum;
    }

    public String getSpecification() {
        return specification;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public int getLineNum() {
        return lineNum;
    }

    /**
     * Analyzes a violations file and returns a set of violations.
     *
     * @param violationsPath The file where violations are located
     * @return A set of violations, each violation is a list containing the specification, a class, and a line number
     */
    public static Set<Violation> parseViolations(String violationsPath) {
        try {
            return Files.readAllLines(new File(violationsPath).toPath())
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
     * @return A set of violation specifications
     */
    public static Set<String> parseViolationSpecs(String violationsPath) {
        return parseViolations(violationsPath).stream().map(Violation::getSpecification).collect(Collectors.toSet());
    }

    /**
     * Parses the string output of a violation into a violation.
     *
     * @param violation Violation line to parse
     * @return Triple of violation specification, class, and line number
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
        String classInfo = classResult.substring(0, classResult.length() - 1);;

        int lineNum = Integer.parseInt(classNameAndLineNum[1].substring(0, classNameAndLineNum[1].indexOf(")")));

        return new Violation(specification, classInfo, lineNum);
    }

    @Override
    public String toString() {
        return "(" + specification + ", " + classInfo + ", " + lineNum + ")";
    }
}
