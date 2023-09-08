package edu.cornell.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Util methods for scripts that set up integration tests.
 */
public class SetUpUtil {
    private File zlcFile;

    public SetUpUtil(File zlcFile) {
        this.zlcFile = zlcFile;
    }

    public void replaceAllInFile(File file, String before, String after) throws IOException {
        if (zlcFile.exists()) {
            Path path = file.toPath();
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(before, after);
            Files.write(path, content.getBytes(charset));
        }
    }

    public void replaceLineInFile(File file, int lineNumber, String newLine) throws IOException {
        if (zlcFile.exists()) {
            Path path = file.toPath();
            Charset charset = StandardCharsets.UTF_8;
            List<String> lines = Files.readAllLines(path, charset);
            
            if (lineNumber >= 0 && lineNumber < lines.size()) {
                lines.set(lineNumber, newLine);
                Files.write(path, lines, charset);
            } else {
                throw new IllegalArgumentException("Invalid line number provided.");
            }
        }
    }
    
}