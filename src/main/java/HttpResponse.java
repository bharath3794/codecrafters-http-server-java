import lombok.ToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@ToString
public class HttpResponse<T> {
    private static String LINE_FEED = "\r\n";
    private ResponseStatus responseStatus;
    private T responseBody;
    private Protocol protocol;
    private double version;
    private Map<String, Object> headers = new HashMap<>();

    public HttpResponse() {
    }

    public HttpResponse(String lineFeed) {
        LINE_FEED = lineFeed;
    }

    public String getResponseStatus() {
        return responseStatus==null ? null : responseStatus.getCode() + " " + responseStatus.getStatus();
    }

    public HttpResponse<T> responseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
        return this;
    }

    public String getHttpVersion() {
        return protocol==null ? null : protocol.toString() + "/" + version;
    }

    public HttpResponse<T> protocol(String protocol) {
        this.protocol = Protocol.valueOf(protocol);
        return this;
    }

    public HttpResponse<T> version(double version) {
        this.version = version;
        return this;
    }

    public HttpResponse<T> header(String key, Object value) {
        assert key != null : "key cannot be null";
        assert value != null : "value cannot be null";
        headers.put(key, value);
        return this;
    }

    public Object getHeader(String key) {
        if (!headers.containsKey(key))
            throw new IllegalArgumentException("Key " + key + " doesn't exist");
        return headers.get(key);
    }

    public T getResponseBody() {
        return responseBody;
    }

    public HttpResponse<T> responseBody(T responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public String createResponse() {
        StringBuilder sb = new StringBuilder();
        //Status Line
        sb.append(this.getHttpVersion())
                .append(" ")
                .append(this.getResponseStatus())
                .append(LINE_FEED);

        //Headers
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            sb.append(header.getKey() + ": " + header.getValue());
            sb.append(LINE_FEED);
        }
        sb.append(LINE_FEED);

        //Response Body
        sb.append(responseBody.toString());

        return sb.toString();
    }
}
