import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public enum CompressionScheme {
    GZIP(1, "gzip");

    private int priority;
    private String scheme;

    CompressionScheme(int priority, String scheme) {
        this.priority = priority;
        this.scheme = scheme;
    }

    public int getPriority() {
        return priority;
    }

    public String getScheme() {
        return scheme;
    }

    public static Stream<CompressionScheme> getPrioritizedCompressionSchemes() {
        return Arrays.stream(CompressionScheme.values()).sorted(Comparator.comparing(CompressionScheme::getPriority));
    }
}
