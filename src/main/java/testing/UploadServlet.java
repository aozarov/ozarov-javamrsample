package testing;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      String bucket = null;
      String filename = null;
      ByteBuffer src = null;
      String mimeType = "text/plain";
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iter = upload.getItemIterator(req);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        InputStream stream = item.openStream();
        switch (item.getFieldName()) {
          case "bucket"	:
            bucket = readString(stream);
            System.out.println("Bucket=" + bucket);
            break;
          case "filename":
            filename = readString(stream);
            System.out.println("filename=" + filename);
            break;
          case "datafile":
            src = readFile(stream);
            System.out.println("File was read...");
            break;
          case "mimetype":
            mimeType = readString(stream);
            System.out.println("Mimetype will be set to " + mimeType);
            break;
        }
      }
      GcsService service = GcsServiceFactory.createGcsService();
      GcsFilename gcsFilename = new GcsFilename(bucket, filename);
      System.out.println("Going to upload: " + gcsFilename);
      GcsFileOptions options = mimeType == null || mimeType.isEmpty() ?
          GcsFileOptions.getDefaultInstance()
          : new GcsFileOptions.Builder().mimeType(mimeType).build();
      service.createOrReplace(gcsFilename, options, src);
      resp.getWriter().println("Done");
    } catch (Exception ex) {
      resp.getWriter().println("Failed: " + ex.toString());
      resp.getWriter().flush();
      ex.printStackTrace();
      throw new ServletException(ex);
    }
  }

  private static String readString(InputStream stream) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] chunk = new byte[1024];
    int count = 0;
    while ((count = stream.read(chunk)) > 0) {
      bytes.write(chunk, 0, count);
    }
    return bytes.toString("UTF-8");
  }

  private static ByteBuffer readFile(InputStream stream) throws IOException, URISyntaxException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] chunk = new byte[1024];
    int total = 0;
    int count = 0;
    while ((count = stream.read(chunk)) > 0) {
      bytes.write(chunk, 0, count);
      total += count;
    }
    System.out.println("KOKO: readFile [" + total + "] bytes");
    /*
    URL url = new URL("https", "storage.googleapis.com", "/a.txt?a=b#hash1");
    System.out.println("KOKO: URL.getAuthority()" + url.getAuthority());
    System.out.println("KOKO: URL.toString()" + url.toString());
    System.out.println("KOKO: URL.toURU()" + url.toURI());
    System.out.println("KOKO: URL.toExternalForm()" + url.toExternalForm());
    System.out.println("KOKO: URL.getQuery()" + url.getQuery());
    System.out.println("KOKO: URL.getRef()" + url.getRef());
*/
    return ByteBuffer.wrap(bytes.toByteArray());
  }
}
