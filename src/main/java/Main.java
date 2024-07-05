import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
  public static void main(String[] args) {
      System.out.println(Arrays.toString(args));
      HttpServer httpServer = new HttpServer(4221, 10);
      httpServer.start();
  }
}
