/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix.util;

import io.atomix.copycat.ConsistencyLevel;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.protocol.Address;
import io.atomix.copycat.session.Session;
import io.atomix.copycat.util.concurrent.Listener;
import io.atomix.copycat.util.concurrent.ThreadContext;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * BSON Copycat client.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class BsonClient implements CopycatClient {
  private final CopycatClient client;
  private final Serializer serializer = Bson.newSerializer();

  public BsonClient(CopycatClient client) {
    this.client = client;
  }

  @Override
  public State state() {
    return client.state();
  }

  @Override
  public Listener<State> onStateChange(Consumer<State> callback) {
    return client.onStateChange(callback);
  }

  @Override
  public ThreadContext context() {
    return client.context();
  }

  @Override
  public Session session() {
    return client.session();
  }

  @Override
  public CompletableFuture<byte[]> submitCommand(byte[] command) {
    return client.submitCommand(command);
  }

  public <T> CompletableFuture<T> submitCommand(Object command, Class<T> type) {
    return client.submitCommand(serializer.writeValue(command))
      .thenApply(result -> serializer.readValue(result, type));
  }

  @Override
  public CompletableFuture<byte[]> submitQuery(byte[] query, ConsistencyLevel consistency) {
    return client.submitQuery(query, consistency);
  }

  public <T> CompletableFuture<T> submitQuery(Object query, Class<T> type) {
    return submitQuery(query, ConsistencyLevel.LINEARIZABLE, type);
  }

  public <T> CompletableFuture<T> submitQuery(Object query, ConsistencyLevel consistency, Class<T> type) {
    return client.submitQuery(serializer.writeValue(query), consistency)
      .thenApply(result -> serializer.readValue(result, type));
  }

  @Override
  public Listener<Void> onEvent(String event, Runnable callback) {
    return client.onEvent(event, callback);
  }

  @Override
  public <T> Listener<T> onEvent(String event, Consumer<T> callback) {
    return client.onEvent(event, callback);
  }

  @Override
  public CompletableFuture<CopycatClient> connect(Collection<Address> members) {
    return client.connect(members);
  }

  @Override
  public CompletableFuture<CopycatClient> recover() {
    return client.recover();
  }

  @Override
  public CompletableFuture<Void> close() {
    return client.close();
  }
}
