package method;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import protocol.Vless;
import protocol.Vmess;
import protocol.Trojan;

public class Editor {

  public static Set<String> Processes(List<String> links, String uuid, String sni, String tag) {
    int batchSize = 10;
    Set<String> values = new HashSet<>();

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    for (int i = 0; i < links.size(); i += batchSize) {
      List<String> batch = links.subList(i, Math.min(i + batchSize, links.size()));
      Runnable task = new Runnable() {
        @Override
        public void run() {
          editor(batch, values, uuid, sni, tag);
        }
      };
      executor.execute(task);
    }

    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return values;
  }

  private static void editor(List<String> batch, Set<String> values, String uuid, String sni, String tag) {
    for (String link : batch) {
      if (link.startsWith("vmess")) {
        link = Vmess.edit(link, uuid, sni, tag);
      } else if (link.startsWith("trojan")) {
        link = Trojan.edit(link, uuid, sni, tag);
      } else if (link.startsWith("vless")) {
        link = Vless.edit(link, uuid, sni, tag);
      }
      if (link != null && !link.isEmpty()) {
        values.add(link);
      }
    }
  }
}