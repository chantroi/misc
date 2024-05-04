package method;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SetProxy {
  private static final String PROXY_URL = System.getenv("PROXY_URL");

  private static final int TIMEOUT_MS = 1000;
  private static final int RETRY_INTERVAL_MS = 1000;
  private static final int MAX_RETRY_COUNT = 3;

  public static boolean testProxy() {
    long startTime = System.currentTimeMillis();
    int retryCount = 0;
    while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(3)) {
      try {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10808));
        HttpURLConnection connection = (HttpURLConnection) new URL("https://www.google.com/generate_204").openConnection(proxy);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
          return true;
        }
      } catch (Exception e) {
        // Ignore and retry
      }
      retryCount++;
      if (retryCount >= MAX_RETRY_COUNT) {
        break;
      }
      try {
        Thread.sleep(RETRY_INTERVAL_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return false;
  }

  public static boolean runProxy() {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(PROXY_URL).openConnection();
      connection.setRequestMethod("GET");
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      String config = response.toString();
      ProcessBuilder processBuilder = new ProcessBuilder("./lite", "-p", "10808", config);
      processBuilder.start();
      return testProxy();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void main(String[] args) {
    boolean proxyTestResult = runProxy();
    System.out.println("Proxy test result: " + (proxyTestResult ? "Success" : "Failed"));
  }
}