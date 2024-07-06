import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static javax.print.attribute.standard.Compression.GZIP;

public class HttpServer {
    private static final Logger LOGGER = Logger.getLogger("HttpServer");
    private static final int DEFAULT_PORT = 4221;
    private ServerSocket serverSocket;
    private final int port;
    private final int numThreads;
    private boolean serverStatus;
    private final ExecutorService executorService;

    public HttpServer() {
        this.port = DEFAULT_PORT;
        this.numThreads = 1;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }

    public HttpServer(int port) {
        this.port = port;
        this.numThreads = 1;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }

    public HttpServer(int port, int numThreads) {
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

    private void handleGetRequest(HttpRequest request, OutputStream outputStream) {
        LOGGER.info("Handling GET request...");
        try {
            // Request Response
            if (Objects.equals(request.getRequestTarget(), "/")) {
                HttpResponse<String> response = new HttpResponse<>();
                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.OK)
                        .responseBody("");
                outputStream.write(response.createResponse().getBytes());
            } else if (request.containsHeader("Accept-Encoding") && request.getRequestTarget().startsWith("/echo/")) {
                HttpResponse<String> response = new HttpResponse<>();
                String responseBody = request.getRequestTarget().substring(6);

                Optional<CompressionScheme> compressionScheme =
                        CompressionScheme.getPrioritizedCompressionSchemes()
                                .filter(s ->
                                        request.getHeader("Accept-Encoding")
                                                .contains(s.getScheme()))
                                .findFirst();
                String scheme = "";
                if (compressionScheme.isPresent()) {
                    scheme = compressionScheme.get().getScheme();
                }


                String hexCompressedString = null;
                int contentLength = 0;
                switch (scheme) {
                    case "gzip" -> {
                        byte[] byteCompressedArr = Util.compressStringToGzipByteArray(responseBody);
                        hexCompressedString = Util.convertByteArrayToHexString(byteCompressedArr);
                        contentLength = byteCompressedArr.length;
                    }

                }
                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.OK)
                        .header("Content-Type", "text/plain");
                compressionScheme.ifPresent(value -> response.header("Content-Encoding", value.getScheme()));
                if (hexCompressedString != null) {
                    response.header("Content-Length", contentLength)
                            .responseBody(hexCompressedString);
                }
                outputStream.write(response.createResponse().getBytes());
            } else if (!request.containsHeader("Accept-Encoding") && request.getRequestTarget().startsWith("/echo/")) {
                HttpResponse<String> response = new HttpResponse<>();

                String responseBody = request.getRequestTarget().substring(6);

                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.OK)
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", responseBody.length())
                        .responseBody(responseBody);
                outputStream.write(response.createResponse().getBytes());
            } else if (request.getRequestTarget().startsWith("/user-agent")) {
                HttpResponse<String> response = new HttpResponse<>();

                String responseBody = request.getHeader("User-Agent");

                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.OK)
                        .header("Content-Type", "text/plain")
                        .header("Content-Length", responseBody.length())
                        .responseBody(responseBody);
                outputStream.write(response.createResponse().getBytes());
            } else if (request.getRequestTarget().startsWith("/files/")) {
                String filePath = Main.directory + request.getRequestTarget().substring(7);
                LOGGER.info("Read FilePath: " + filePath);
                HttpResponse<String> response = new HttpResponse<>();
                if (Util.checkFileExists(filePath)) {
                    String contents = Util.readFileToString(filePath);
                    long fileSize = Util.getFileSize(filePath);

                    response.protocol(request.getProtocol().toString())
                            .version(request.getVersion())
                            .responseStatus(ResponseStatus.OK)
                            .header("Content-Type", "application/octet-stream")
                            .header("Content-Length", fileSize)
                            .responseBody(contents);
                    outputStream.write(response.createResponse().getBytes());
                } else {
                    response.protocol(request.getProtocol().toString())
                            .version(request.getVersion())
                            .responseStatus(ResponseStatus.NOT_FOUND)
                            .responseBody("");
                    outputStream.write(response.createResponse().getBytes());
                }
            } else {
                HttpResponse<String> response = new HttpResponse<>();

                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.NOT_FOUND)
                        .responseBody("");
                outputStream.write(response.createResponse().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePostRequest(HttpRequest request, OutputStream outputStream) {
        LOGGER.info("Handling POST request...");
        try {
            if (request.getRequestTarget().startsWith("/files/")) {
                HttpResponse<String> response = new HttpResponse<>();
                String filePath = Main.directory + request.getRequestTarget().substring(7);
                LOGGER.info("FilePath: " + filePath);

                Util.writeToFileUsingBufferedWriter(filePath, request.getRequestBody());

                response.protocol(request.getProtocol().toString())
                        .version(request.getVersion())
                        .responseStatus(ResponseStatus.CREATED);
                outputStream.write(response.createResponse().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(Socket clientSocket) {
        LOGGER.info("Handling request on thread: " + Thread.currentThread().getName());
        try (
                final InputStream inputStream = clientSocket.getInputStream();
                final OutputStream outputStream = clientSocket.getOutputStream();
        ) {
            // Read HttpRequest
            HttpRequest request = HttpRequest.fromInputStream(inputStream);

            switch (request.getRequestType()) {
                case GET -> handleGetRequest(request, outputStream);
                case POST -> handlePostRequest(request, outputStream);
            }

            outputStream.flush();
        } catch (IOException e) {
            LOGGER.severe("IOException occurred with message: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                LOGGER.severe("IOException while closing client socket: " + e.getMessage());
            }
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
