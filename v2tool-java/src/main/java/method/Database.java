package method;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

public class Database {
  private MongoCollection<Document> notes;

  public void Mongo() {
    String dbUrl = System.getenv("MONGO_URL");
    MongoClient mongoClient = MongoClients.create(dbUrl);
    MongoDatabase database = mongoClient.getDatabase("mo9973_notes");
    this.notes = database.getCollection("notes");
  }

  public List<String> get(String note) throws Exception {
    Document result = notes.find(Filters.eq("id", note)).first();

    if (result != null) {
      List<String> urls = (List<String>) result.get("urls");
      return urls;
    } else {
      throw new Exception("Không tìm thấy dữ liệu");
    }
  }

  public static List<String> getData(String note) {
    String dbUrl = System.getenv("MONGO_URL");
    MongoClient mongoClient = MongoClients.create(dbUrl);
    MongoDatabase database = mongoClient.getDatabase("mo9973_notes");
    MongoCollection<Document> notes = database.getCollection("notes");

    Document result = notes.find(Filters.eq("_id", note))
                 .projection(Projections.include("urls"))
                 .first();

    List<String> urls = new ArrayList<>();
    if (result != null) {
      urls = (List<String>) result.get("urls");
    }
    return urls;
  }
}