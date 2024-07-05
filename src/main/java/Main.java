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

      HttpServer2 httpServer = new HttpServer2(4221, 10);
      httpServer.start();
  }
}
