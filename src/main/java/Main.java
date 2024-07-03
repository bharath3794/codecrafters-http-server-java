import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(4221);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       try (var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
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
               clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
           } else if (requestLineArr.length != 0 && requestLineArr[1] != null && requestLineArr[1].startsWith("/echo/")) {
               String responseBody = requestLineArr[1].substring(6);
               clientSocket.getOutputStream().write(String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", responseBody.length(), responseBody).getBytes());
           } else if (requestLineArr.length != 0 && requestLineArr[1] != null && requestLineArr[1].startsWith("/user-agent")) {
               String responseBody = headers.getOrDefault("User-Agent", "");
               clientSocket.getOutputStream().write(String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", responseBody.length(), responseBody).getBytes());
           } else {
               clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
           }
       }
       System.out.println("accepted new connection");
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
