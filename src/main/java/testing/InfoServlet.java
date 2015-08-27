package testing;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InfoServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = req.getParameter("bucket");
      String filename = req.getParameter("filename");
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename gcsFilename = new GcsFilename(bucket, filename);
      GcsFileMetadata metadata = service.getMetadata(gcsFilename);
      if (metadata == null) {
        resp.getWriter().println("File not found");
      } else {
        resp.getWriter().println(metadata.toString());
      }
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
