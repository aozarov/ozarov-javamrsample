package testing;

//import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
//import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.storage.Storage;
//import com.google.api.services.storage.Storage.Objects;
//import com.google.api.services.storage.model.StorageObject;
//import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ListServlet extends HttpServlet {

  public static final List<String> OAUTH_SCOPES =
      ImmutableList.of("https://www.googleapis.com/auth/devstorage.read_write");

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = req.getParameter("bucket");
      /*
      AppIdentityCredential cred = new AppIdentityCredential(OAUTH_SCOPES);
      Storage storage = new Storage.Builder(new UrlFetchTransport(), new JacksonFactory(), cred)
        .setApplicationName(SystemProperty.applicationId.get()).build();
      Objects.List list = storage.objects().list(bucket);
      resp.getWriter().println("<html><body>");
      resp.getWriter().println("<h2>Listing 1</h2><ul>");
      for (StorageObject o : list.execute().getItems()) {
        resp.getWriter().println("<li>" + o.getName() + " -> " + o);
      }
      */

      PrintWriter writer = resp.getWriter();
      ListOptions options = null;
      listAndDisplay(writer, bucket, options);
      options = ListOptions.DEFAULT;
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setPrefix("s").build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setPrefix("dir1/dir2").build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setPrefix("dir1/dir2/").build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setRecursive(false).build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setRecursive(false).setPrefix("s").build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setRecursive(false).setPrefix("dir1/dir2").build();
      listAndDisplay(writer, bucket, options);
      options = new ListOptions.Builder().setRecursive(false).setPrefix("dir1/dir2/").build();
      listAndDisplay(writer, bucket, options);
    } catch (Exception ex) {
      resp.getWriter().println("<h1>Failed: " + ex.toString() + "</h1>");
      resp.getWriter().println("<h2>Details</h2><pre>");
      ex.printStackTrace(resp.getWriter());
      resp.getWriter().println("</pre>");
    }
    resp.getWriter().println("</body></html>");
  }

  private void listAndDisplay(PrintWriter writer, String bucket, ListOptions options)
      throws IOException {
    writer.println("<h2>Listing " + bucket + " using " + options + "</h2><ul>");
    GcsService service = GcsServiceFactory.createGcsService();
    ListResult result = service.list(bucket, options);
    while (result.hasNext()) {
      ListItem li = result.next();
      writer.println("<li>" + li);
    }
    writer.println("</ul><hr>");
  }
}
