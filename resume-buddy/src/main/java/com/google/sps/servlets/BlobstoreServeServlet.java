import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/blobstore-serve")
public class BlobstoreServeServlet extends HttpServlet {
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Gets the logged in user's email, finds the PDF blob and serves it
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(Integer.MAX_VALUE);
    String email = userService.getCurrentUser().getEmail();
    Query query = new Query("Match");

    PreparedQuery results = datastore.prepare(query);
    List<Entity> entityList = results.asList(fetchOptions);
    String userBlobKeyString = "";

    for (Entity pair : entityList) {
      if ((pair.getProperty("reviewee").toString().equals(email))
          || (pair.getProperty("reviewer").toString().equals(email))) {
        userBlobKeyString = pair.getProperty("resumeBlobKey").toString();
      }
    }
    //TODO: (sesexton) Included more logic if the user is both a reviewee and a reviewer

    BlobKey userBlobKey = new BlobKey(userBlobKeyString);
    blobstoreService.serve(userBlobKey, response);
  }
}
