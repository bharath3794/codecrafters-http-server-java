import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Util {
    public static String readFileToString(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            content.append(line).append(System.lineSeparator());
        }
        return content.toString();
    }

    public static boolean checkFileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    public static long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.size(path);
    }
}
