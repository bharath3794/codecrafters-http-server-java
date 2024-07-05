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
    public static String directory = null;
  public static void main(String[] args) {
      if (args.length > 1 && args[0].equals("--directory")) {
          directory = args[1];
      }
      HttpServer httpServer = new HttpServer(4221, 10);
      httpServer.start();
  }
}
