package protocol;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Vmess {

  public static String edit(String link, String setUUID, String setSNI, String setTag) {
    String code = link.split("://")[1];
    byte[] decodedBytes = Base64.getDecoder().decode(code);
    String configString = new String(decodedBytes);
    Map<String, String> config = new HashMap<>();
    try {
      config = new ObjectMapper().readValue(configString, new TypeReference<HashMap<String,String>>(){});
    } catch (IOException e) {
      e.printStackTrace();
    }

    String ip = config.get("add");
    String net = config.get("net");
    if (ip.equals("127.0.0.1") || ip.equals("1.1.1.1") || ip.equals("0.0.0.0") || ip.equals("8.8.8.8")) {
      return null;
    }

    if (setTag != null) {
      config.put("ps", setTag);
    }

    if (setUUID != null) {
      config.put("id", setUUID);
    }

    if (setSNI != null) {
      if (net.equals("tcp")) {
        config.put("sni", setSNI);
      } else {
        config.put("host", setSNI);
      }
    }

    String updatedConfigString = new ObjectMapper().writeValueAsString(config);
    byte[] encodedBytes = Base64.getEncoder().encode(updatedConfigString.getBytes());
    String updatedCode = new String(encodedBytes);

    return "vmess" + "://" + updatedCode;
  }
}