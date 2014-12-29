package com.google.appengine.demos.mapreduce.randomcollisions;

import static com.google.appengine.demos.mapreduce.randomcollisions.CollisionFindingServlet.createMapReduceSpec;
import static com.google.appengine.demos.mapreduce.randomcollisions.CollisionFindingServlet.getBucketParam;
import static com.google.appengine.demos.mapreduce.randomcollisions.CollisionFindingServlet.getLongParam;
import static com.google.appengine.demos.mapreduce.randomcollisions.CollisionFindingServlet.getSettings;
import static com.google.appengine.demos.mapreduce.randomcollisions.CollisionFindingServlet.getStringParam;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.mapreduce.GoogleCloudStorageFileSet;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.JobSetting;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is an alternative to {@link CollisionFindingServlet} that uses Pipelines to start the
 * MapReduce.
 *
 * This example shows running a follow up task after the MapReduce has run.
 */
public class UsingPipelineServlet extends HttpServlet {
  private static final long serialVersionUID = -8159877366348539461L;

  private static final Logger log = Logger.getLogger(UsingPipelineServlet.class.getName());
  private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
  private final UserService userService = UserServiceFactory.getUserService();
  private final SecureRandom random = new SecureRandom();

  /**
   * A two step pipeline: 1. Run a MapReduce 2. Run LogFileNamesJob with the output of the
   * MapReduce.
   */
  private static class MyPipelineJob extends Job0<Void> {
    private static final long serialVersionUID = 1954542676168374323L;

    private final String bucket;
    private final long start;
    private final long limit;
    private final int shards;

    MyPipelineJob(String bucket, long start, long limit, int shards) {
      this.bucket = bucket;
      this.start = start;
      this.limit = limit;
      this.shards = shards;
    }

    @Override
    public Value<Void> run() throws Exception {
      MapReduceSpecification<Long, Integer, Integer, ArrayList<Integer>, GoogleCloudStorageFileSet>
      spec = createMapReduceSpec(bucket, start, limit, shards);
      MapReduceSettings settings = getSettings(bucket, null, null, null);
      // [START start_as_pipeline]
      FutureValue<MapReduceResult<GoogleCloudStorageFileSet>> mapReduceResult =
          futureCall(new MapReduceJob<>(spec, settings));
      // [END start_as_pipeline]
      return futureCall(new LogFileNamesJob(), mapReduceResult);
    }
  }

  private static class LogFileNamesJob extends
      Job1<Void, MapReduceResult<GoogleCloudStorageFileSet>> {
    private static final long serialVersionUID = 6277239748168293296L;

    @Override
    public Value<Void> run(MapReduceResult<GoogleCloudStorageFileSet> result) {
      for (GcsFilename name : result.getOutputResult().getFiles()) {
        log.info("Output stored to file: " + name);
      }
      return null;
    }
  }

  private void writeResponse(HttpServletResponse resp) throws IOException {
    String token = String.valueOf(random.nextLong() & Long.MAX_VALUE);
    memcache.put(userService.getCurrentUser().getUserId() + " " + token, true);

    try (PrintWriter pw = new PrintWriter(resp.getOutputStream())) {
      pw.println("<html><body>"
          + "<br><form method='post'><input type='hidden' name='token' value='" + token + "'>"
          + "Pipeline example: <br />"
          + "Start: <input name='start' value='0'>"
          + "Limit: <input name='limit' value='10000'>"
          + "GCS bucket: <input name='gcs_bucket'> (Leave empty to use the app's default bucket)"
          + "Queue: <input name='queue'> (Leave empty to use default queue)"
          + "<br /> <input type='submit' value='Create, Count, and Delete'>"
          + "</div> </form> </body></html>");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (userService.getCurrentUser() == null) {
      log.info("no user");
      return;
    }
    writeResponse(resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (userService.getCurrentUser() == null) {
      log.info("no user");
      return;
    }
    String token = req.getParameter("token");
    if (!memcache.delete(userService.getCurrentUser().getUserId() + " " + token)) {
      throw new RuntimeException("Bad token, try again: " + token);
    }
    String bucket = getBucketParam(req);
    String queue = getStringParam(req, "queue");
    long start = getLongParam(req, "start", 0);
    long limit = getLongParam(req, "limit", 100 * 1000 * 1000);
    int shards = Math.max(1, Math.min(100, Ints.saturatedCast(getLongParam(req, "shards", 30))));
    PipelineService service = PipelineServiceFactory.newPipelineService();
    String pipelineId = service.startNewPipeline(new MyPipelineJob(bucket, start, limit, shards),
        new JobSetting.OnQueue(queue));
    resp.sendRedirect("/_ah/pipeline/status.html?root=" + pipelineId);
  }
}
