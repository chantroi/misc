package method;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
  private static final String USER_AGENT = "v2rayNG/*.*.*";
  private static final int TIMEOUT_SECONDS = 5;
  private static final int MAX_THREADS = 10;

  public static List<String> getResponse(String url) {
    List<String> links = new ArrayList<>();
    String response = makeRequest(url);
    if (response != null) {
      if (response.contains("vmess:") || response.contains("trojan:") || response.contains("vless:")) {
        extractLinks(response, links);
      } else if (response.contains("http:") || response.contains("https:")) {
        List<String> subUrls = extractUrls(response);
        links.addAll(getResponses(subUrls));
      } else {
        String decodedLine = decodeBase64(response);
        if (decodedLine != null && (decodedLine.contains("vmess:") || decodedLine.contains("trojan:") || decodedLine.contains("vless:"))) {
          extractLinks(decodedLine, links);
        }
      }
    }
    return links;
  }

  private static String makeRequest(String url) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", USER_AGENT);
      connection.setConnectTimeout(TIMEOUT_SECONDS * 1000);
      connection.setReadTimeout(TIMEOUT_SECONDS * 1000);
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();
        return response.toString();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static List<String> extractUrls(String text) {
    List<String> urls = new ArrayList<>();
    Pattern pattern = Pattern.compile("https?://(?:[-\\w.]|(?:%[\\da-fA-F]{2}))+");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      urls.add(matcher.group());
    }
    return urls;
  }

  private static void extractLinks(String text, List<String> links) {
    String[] lines = text.split("\n");
    for (String line : lines) {
      if (line.contains("vmess:") || line.contains("trojan:") || line.contains("vless:")) {
        links.add(line);
      }
    }
  }

  private static String decodeBase64(String encoded) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(encoded);
      return new String(decodedBytes, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<String> getResponses(List<String> urls) {
    List<String> links = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    for (String url : urls) {
      executor.submit(() -> {
        List<String> subLinks = getResponse(url);
        synchronized (links) {
          links.addAll(subLinks);
        }
      });
    }
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return links;
  }
}