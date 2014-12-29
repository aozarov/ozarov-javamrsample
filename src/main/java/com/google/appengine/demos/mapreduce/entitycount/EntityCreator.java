// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.appengine.demos.mapreduce.entitycount;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Creates random entities.
 *
 * @author ohler@google.com (Christian Ohler)
 */
class EntityCreator extends MapOnlyMapper<Long, Entity> {

  private static final long serialVersionUID = 409204195454478863L;
  private static final Logger log = Logger.getLogger(EntityCreator.class.getName());

  private final String kind;
  private final int payloadBytesPerEntity;
  private final Random random = new Random();
  private StringBuffer stBuffer;

  public EntityCreator(String kind, int payloadBytesPerEntity) {
    this.kind = checkNotNull(kind, "Null kind");
    this.payloadBytesPerEntity = payloadBytesPerEntity;
  }

  private String randomString(int length) {
    StringBuilder out = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      out.append((char) ('a' + random.nextInt(26)));
    }
    return out.toString();
  }

  @Override
  public void beginSlice() {
    if (stBuffer != null) {
      log.info("beginSlice - discarding old bytes [" + stBuffer.length() + "]");
    }
    stBuffer = new StringBuffer();
  }

  @Override
  public void endSlice() {
    log.info("endSlice - preparing to save bytes [" + stBuffer.length() + "]");
  }

  @Override
  public void map(Long ignored) {
    String name = getContext().getShardNumber() + "_" + (random.nextLong() & Long.MAX_VALUE);
    Entity entity = new Entity(kind, name);
    String randomString = randomString(payloadBytesPerEntity);
    stBuffer.append(randomString);
    log.info("map - added " + payloadBytesPerEntity + " to bytes [" + stBuffer.length() + "]");
    entity.setProperty("payload", new Text(randomString));
    emit(entity);
  }
}
