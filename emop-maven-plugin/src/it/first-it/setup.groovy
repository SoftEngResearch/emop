import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

File startsDir = new File(basedir, ".starts");

print("current dir: " + basedir.getAbsolutePath());

if (!(startsDir.exists() && startsDir.isDirectory())) {
    print("Notification: .starts exists");
} else {
    print(".starts does not exist");

    // Function to replace a specific line in a file

    // Example usage: Replace the 5th line of a file with "New Content"
    File someFile = new File(basedir, "src/test/java/first/FirstTest.java");
    replaceLineInFile(someFile, 15, "     Set out = first.getSet();");
}


  void replaceLineInFile(File file, int lineNumber, String newLine) {
        try {
            Path path = file.toPath();
            Charset charset = StandardCharsets.UTF_8;
            List<String> lines = Files.readAllLines(path, charset);

            if (lineNumber >= 0 && lineNumber < lines.size()) {
                lines.set(lineNumber, newLine);
                Files.write(path, lines, charset);
            } else {
                print("Invalid line number provided.");
            }
        } catch (IOException e) {
            print("Error: " + e.getMessage());
        }
    }
