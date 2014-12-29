package testing;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ComposeServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = req.getParameter("bucket");
      String source = req.getParameter("source");
      String destination = req.getParameter("destination");
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename destFilename = new GcsFilename(bucket, destination);
      List<String> srcFilenames = Lists.newArrayList(source.split(" *, *"));
      service.compose(srcFilenames, destFilename);
      GcsFilename lastFile =
          new GcsFilename(bucket, srcFilenames.get(srcFilenames.size() - 1));
      GcsFileMetadata metadata = service.getMetadata(lastFile);
      resp.getWriter().println("File Composed.");
      service.update(destFilename, metadata.getOptions());
      resp.getWriter().println("Metadata updated Composed.");
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }
}
