package protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Vless {

  public static String edit(String urlString, String setUUID, String setSNI, String setTag) {
    try {
      URI uri = new URI(urlString);
      String query = uri.getQuery();
      Map<String, String> queryParams = new HashMap<>();
      if (query != null && !query.isEmpty()) {
        for (String param : query.split("&")) {
          String[] pair = param.split("=");
          queryParams.put(pair[0], pair[1]);
        }
      }

      String[] netloc = uri.getHost().split("@");
      String ip = netloc[1].split(":")[0];
      if (ip.equals("127.0.0.1") || ip.equals("1.1.1.1") || ip.equals("0.0.0.0") || ip.equals("8.8.8.8")) {
        return null;
      }

      if (setUUID != null) {
        netloc[0] = setUUID;
        uri = new URI(uri.getScheme(), uri.getUserInfo(), String.join("@", netloc), uri.getPort(),
            uri.getPath(), uri.getQuery(), uri.getFragment());
      }

      if (setSNI != null) {
        if (queryParams.containsKey("type") && queryParams.get("type").equals("tcp")) {
          queryParams.put("sni", setSNI);
        } else {
          queryParams.put("host", setSNI);
        }
      }

      if (setTag != null) {
        uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
            getQueryString(queryParams), setTag);
      }

      return uri.toString();

    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static String getQueryString(Map<String, String> params) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(entry.getKey()).append("=").append(entry.getValue());
    }
    return sb.toString();
  }
}