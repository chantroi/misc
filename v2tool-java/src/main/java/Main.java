import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import method.SetProxy;
import method.Database;
import method.Request;
import method.Editor;

@SpringBootApplication
@RestController
public class Main {

  private final RestTemplate restTemplate = new RestTemplate();

  @GetMapping("/")
  public ResponseEntity<String> processQuery(@RequestParam String url,
      @RequestParam(required = false) String uuid,
      @RequestParam(required = false) String sni,
      @RequestParam(required = false) String tag) {
    SetProxy.runProxy();
    String queryUrl = UriComponentsBuilder.fromUriString(url).build().toString();
    List<String> listLinks = Request.getResponse(queryUrl);
    Set<String> processedLinks = Editor.Processes(listLinks, uuid, sni, tag);
    String resultLinks = String.join("\n", processedLinks);
    return ResponseEntity.ok().body(encodeBase64(resultLinks));
  }

  @GetMapping("/get/{filename}")
  public ResponseEntity<String> processAllConfig(@PathVariable String filename,
      @RequestParam(required = false) String uuid,
      @RequestParam(required = false) String sni,
      @RequestParam(required = false) String tag) {
    SetProxy.runProxy();
    List<String> urls = Database.getData(filename);
    List<String> listLinks = Request.getResponses(urls);
    Set<String> processedLinks = Editor.Processes(listLinks, uuid, sni, tag);
    String resultLinks = String.join("\n", processedLinks);
    return ResponseEntity.ok().body(encodeBase64(resultLinks));
  }

  private String encodeBase64(String data) {
    return Base64.getEncoder().encodeToString(data.getBytes());
  }

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}