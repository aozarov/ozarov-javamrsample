package testing;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({"serial", "deprecation"})
public class FileApiServlet extends HttpServlet {
  
   public static final String FILENAME = "test-file.txt";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    FileService service = FileServiceFactory.getFileService();
    AppEngineFile file = service.createNewBlobFile("Blabla", FILENAME);
    FileWriteChannel channel = service.openWriteChannel(file, true);
    channel.write(ByteBuffer.wrap("Foo".getBytes()));
    channel.closeFinally();
  }
}