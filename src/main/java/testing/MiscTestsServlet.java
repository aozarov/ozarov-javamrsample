package testing;

import com.google.gcloud.datastore.DatastoreService;
import com.google.gcloud.datastore.DatastoreServiceFactory;
import com.google.gcloud.datastore.DatastoreServiceOptions;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.Key;
import com.google.gcloud.datastore.KeyFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MiscTestsServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      //Queue queue = QueueFactory.getQueue("bad-queue");
      //QueueStatistics stats = queue.fetchStatistics();
      //resp.getWriter().println(stats.getQueueName());
      //resp.getWriter().println(stats.getEnforcedRate());

      DatastoreServiceOptions options = DatastoreServiceOptions.builder().host("localhost:8080").build();
      DatastoreService datastore = DatastoreServiceFactory.getDefault(options);
      KeyFactory keyFactory = new KeyFactory(datastore);
      Key key = keyFactory.kind("new-kind").allocateId();
      datastore.add(Entity.builder(key).set("txt", "hello world").build());
      Entity entity = datastore.get(key);
      System.out.println("KOKO: " + entity);
      resp.getWriter().println("entity: " + entity);
      /*
      System.out.println("KOKO: " + System.getProperty("com.google.appengine.application.id"));
      AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();
      GetAccessTokenResult token = appIdentityService.getAccessToken(
          Collections.singleton("https://www.googleapis.com/auth/datastore"));
      System.out.println("KOKO. got token: " + token.toString() + ", expires on: " + token.getExpirationTime());

      String bucket = appIdentityService.getDefaultGcsBucketName();
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename filename = new GcsFilename(bucket, req.getParameter("file"));
      GcsFilename temp = new GcsFilename(bucket, "temp");
      int start = Integer.parseInt(req.getParameter("start"));
      int end = Integer.parseInt(req.getParameter("end"));
      for (int i = start; i <= end; i++) {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode(CharBuffer.wrap("\n" + i ));
        GcsOutputChannel out1 = service.createOrReplace(temp, GcsFileOptions.getDefaultInstance());
        out1.write(buffer1);
        out1.close();
        List<String> files = ImmutableList.of(filename.getObjectName(), "temp");
        service.compose(files, filename);
      }
      resp.getWriter().println("Added " + start + " to " + end + " to " + filename);

      /*
      NamespaceManager.set("ns1");
      service.createOrReplace(filename, GcsFileOptions.getDefaultInstance(), buffer);
      GcsFileMetadata metadata = service.getMetadata(filename);
      System.out.println("Metadata.1 " + metadata);
      if (metadata == null) {
        throw new RuntimeException("Metadata null");
      }
      NamespaceManager.set("ns2");
      metadata = service.getMetadata(filename);
      System.out.println("Metadata.2 " + metadata);
      if (metadata == null) {
        throw new RuntimeException("Metadata null");
      }
      NamespaceManager.set("");
      metadata = service.getMetadata(filename);
      System.out.println("Metadata.3 " + metadata);
      if (metadata == null) {
        throw new RuntimeException("Metadata null");
      }
      */
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
