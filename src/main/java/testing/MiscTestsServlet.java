package testing;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.gcloud.AuthCredentials;
import com.google.gcloud.datastore.Datastore;
import com.google.gcloud.datastore.DatastoreFactory;
import com.google.gcloud.datastore.DatastoreOptions;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.Key;
import com.google.gcloud.datastore.KeyFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;

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
    if (true) {

      try {
        System.out.println("before getting delegate");
        Object delegate = ApiProxy.getDelegate();
        System.out.println("Delegate: " + delegate);
        Method m = delegate.getClass().getDeclaredMethod("getService", String.class);
        m.setAccessible(true);
        Object bs = m.invoke(delegate, "blobstore");
        System.out.println("blobstore - " + bs);
        Field f = bs.getClass().getDeclaredField("blobStorage");
        f.setAccessible(true);
        Object storage = f.get(bs);
        System.out.println(storage);
        if (storage != null)
          return;
      } catch (Exception ex) {
        ex.printStackTrace();
      }


      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename name = new GcsFilename("bucket", "file");
      ByteBuffer content = StandardCharsets.UTF_8.encode("hello world");
      service.createOrReplace(name, GcsFileOptions.getDefaultInstance(), content);
      System.out.printf("Wrote file!!!");
      return;
    }
    ModulesService modulesService = ModulesServiceFactory.getModulesService();
    System.out.println("koko1");
    String instanceId = modulesService.getCurrentInstanceId();
    System.out.println("koko.instanceId: " + instanceId);
    String hostname = modulesService.getInstanceHostname(null, null, instanceId);
    System.out.println("koko.hostname: " + hostname);
    try {
      String password = req.getParameter("password");
      if (password == null) {
        resp.getWriter().println("Missing password param");
        return;
      }
      char[] passwordChars = password.toCharArray();
      KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(getClass().getResourceAsStream("ozarov-javamrsample.p12"), passwordChars);
      PrivateKey privateKey = (PrivateKey) keystore.getKey("1", passwordChars);
      AuthCredentials authCredentials = AuthCredentials.createFor(
          "189024820947-qvtj1o3r7hl8gqhuujt8gchjggjqla1v@developer.gserviceaccount.com", privateKey);
      DatastoreOptions options = DatastoreOptions.builder()
          .authCredentials(authCredentials)
          .projectId("ozarov-javamrsample")
          .build();
      Datastore datastore = DatastoreFactory.instance().get(options);
      KeyFactory keyFactory = datastore.newKeyFactory();
      Key key = datastore.allocateId(keyFactory.kind("new-kind").newKey());
      datastore.add(Entity.builder(key).set("txt", "hello world1").build());
      Entity entity = datastore.get(key);
      resp.getWriter().println("(1) entity: " + entity);

      options = DatastoreOptions.builder().build();
      datastore = DatastoreFactory.instance().get(options);
      keyFactory = datastore.newKeyFactory();
      key = datastore.allocateId(keyFactory.kind("new-kind").newKey());
      datastore.add(Entity.builder(key).set("txt", "hello world2").build());
      entity = datastore.get(key);
      resp.getWriter().println("(2) entity: " + entity);

      /*
      //Queue queue = QueueFactory.getQueue("bad-queue");
      //QueueStatistics stats = queue.fetchStatistics();
      //resp.getWriter().println(stats.getQueueName());
      //resp.getWriter().println(stats.getEnforcedRate());

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
