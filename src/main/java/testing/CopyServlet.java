package testing;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CopyServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = req.getParameter("bucket");
      String source = req.getParameter("source");
      String destination = req.getParameter("destination");
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename destFilename = new GcsFilename(bucket, destination);
      GcsFilename sourceFilename = new GcsFilename(bucket, source);
      service.copy(sourceFilename, destFilename);
      resp.getWriter().println("File Copied.");
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
