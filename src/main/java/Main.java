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
    public static String directory = "C:/Users/vtall/Downloads/";
  public static void main(String[] args) {
//      if (args.length == 1) {
//          Main.directory = args[0];
//      }
      HttpServer httpServer = new HttpServer(4221, 10);
      httpServer.start();
  }
}
