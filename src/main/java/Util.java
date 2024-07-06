import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPOutputStream;

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

    public static void writeToFileUsingBufferedWriter(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    public static byte[] compressStringToGzipByteArray(final String s) throws IOException {
        if (s == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        ) {
            gzipOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String convertByteArrayToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X ", b));
        }

        // Remove the trailing space
        if (!hexString.isEmpty()) {
            hexString.setLength(hexString.length() - 1);
        }
        return hexString.toString();
    }
}
