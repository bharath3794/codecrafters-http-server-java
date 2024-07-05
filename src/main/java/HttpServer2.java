import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpServer2 {
    private static final Logger LOGGER = Logger.getLogger("HttpServer2");
    private static final int DEFAULT_PORT = 4221;
    private ServerSocket serverSocket;
    private final int port;
    private final int numThreads;
    private boolean serverStatus;
    private final ExecutorService executorService;

    public HttpServer2() {
        this.port = DEFAULT_PORT;
        this.numThreads = 1;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }

    public HttpServer2(int port) {
        this.port = port;
        this.numThreads = 1;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }

    public HttpServer2(int port, int numThreads) {
        this.port = port;
        this.numThreads = numThreads;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);

            this.serverStatus = true;

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            LOGGER.info("Server is listening on port " + this.port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("New Client connected on port " + clientSocket.getPort());
                executorService.submit(() -> handleRequest(clientSocket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(Socket clientSocket) {
        LOGGER.info("Handling request on thread: " + Thread.currentThread().getName());
        try (
                final BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final OutputStream outputStream = clientSocket.getOutputStream();
        ) {
            // Read Request Line
            String[] requestLineArr = reader.readLine().split(" ");

            // Read Headers
            Map<String, String> headers = new HashMap<>();
            String line = null;
            while (!(line = reader.readLine()).isEmpty()) {
                String[] keyValuePair = line.split(": ");
                if (keyValuePair.length == 2)
                    headers.put(keyValuePair[0], keyValuePair[1]);
            }

            // Request Response
            if (requestLineArr.length != 0 && Objects.equals(requestLineArr[1], "/")) {
                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (requestLineArr.length != 0 && requestLineArr[1] != null && requestLineArr[1].startsWith("/echo/")) {
                String responseBody = requestLineArr[1].substring(6);
                outputStream.write(String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", responseBody.length(), responseBody).getBytes());
            } else if (requestLineArr.length != 0 && requestLineArr[1] != null && requestLineArr[1].startsWith("/user-agent")) {
                String responseBody = headers.getOrDefault("User-Agent", "");
                outputStream.write(String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", responseBody.length(), responseBody).getBytes());
            } else {
                outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            outputStream.flush();
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (!serverStatus) {
            throw new IllegalStateException("Server is not running");
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
