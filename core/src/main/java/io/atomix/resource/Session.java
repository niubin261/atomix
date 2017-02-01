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
package io.atomix.resource;

import io.atomix.copycat.session.ClosedSessionException;

import java.util.concurrent.CompletableFuture;

/**
 * Resource session.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface Session {

  /**
   * Returns the session ID.
   *
   * @return The session ID.
   */
  long id();

  /**
   * Publishes a {@code null} named event to the session.
   * <p>
   * When an event is published via the {@link io.atomix.copycat.session.Session}, it is sent to the other side of the session's
   * connection. Events can only be sent from a server-side replicated state machine to a client. Attempts
   * to send events from the client-side of the session will result in the event being handled by the client,
   * Sessions guarantee serializable consistency. If an event is sent from a Raft server to a client that is
   * disconnected or otherwise can't receive the event, the event will be resent once the client connects to
   * another server as long as its session has not expired.
   * <p>
   * Event messages must be serializable.
   * <p>
   * The returned {@link CompletableFuture} will be completed once the {@code event} has been sent
   * but not necessarily received by the other side of the connection. In the event of a network or other
   * failure, the message may be resent.
   *
   * @param event The event to publish.
   * @return A completable future to be called once the event has been published.
   * @throws NullPointerException If {@code event} is {@code null}
   * @throws ClosedSessionException If the session is closed
   */
  io.atomix.copycat.session.Session publish(String event);

  /**
   * Publishes an event to the session.
   * <p>
   * When an event is published via the {@link io.atomix.copycat.session.Session}, it is sent to the other side of the session's
   * connection. Events can only be sent from a server-side replicated state machine to a client. Attempts
   * to send events from the client-side of the session will result in the event being handled by the client,
   * Sessions guarantee serializable consistency. If an event is sent from a Raft server to a client that is
   * disconnected or otherwise can't receive the event, the event will be resent once the client connects to
   * another server as long as its session has not expired.
   * <p>
   * The returned {@link CompletableFuture} will be completed once the {@code event} has been sent
   * but not necessarily received by the other side of the connection. In the event of a network or other
   * failure, the message may be resent.
   *
   * @param event The event to publish.
   * @param message The event message.
   * @return A completable future to be called once the event has been published.
   * @throws NullPointerException If {@code event} is {@code null}
   * @throws ClosedSessionException If the session is closed
   */
  io.atomix.copycat.session.Session publish(String event, Object message);

}
