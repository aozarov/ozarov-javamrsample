package testing;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = req.getParameter("bucket");
      String filename = req.getParameter("filename");
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename gcsFilename = new GcsFilename(bucket, filename);
      ByteBuffer bytes = ByteBuffer.allocate(1024);
      try (GcsInputChannel reader = service.openReadChannel(gcsFilename, 0)) {
        while (reader.read(bytes) >= 0) {
          bytes.flip();
          String value = UTF_8.decode(bytes).toString();
          resp.getWriter().print(value);
          bytes.clear();
        }
      }
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
