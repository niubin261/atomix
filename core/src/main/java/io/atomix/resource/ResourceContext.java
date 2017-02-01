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

import io.atomix.copycat.server.session.Sessions;
import io.atomix.copycat.util.concurrent.Scheduled;
import org.slf4j.Logger;

import java.io.Closeable;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Resource context.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface ResourceContext extends Executor, Closeable {

  /**
   * Returns the current state machine index.
   * <p>
   * The state index is indicative of the index of the current command
   * being applied to the server state machine. If a query is being applied,
   * the index of the last command applied will be used.
   *
   * @return The current state machine index.
   */
  long index();

  /**
   * Returns the state machine clock.
   *
   * @return The state machine clock.
   */
  Clock clock();

  /**
   * Returns the state machine sessions.
   *
   * @return The state machine sessions.
   */
  Sessions sessions();

  /**
   * Returns the context logger.
   *
   * @return The context logger.
   */
  Logger logger();

  /**
   * Executes the given callback on the context.
   *
   * @param callback The callback to execute on the context.
   */
  @Override
  void execute(Runnable callback);

  /**
   * Schedules a runnable on the context.
   *
   * @param callback The callback to schedule.
   * @param delay The delay at which to schedule the runnable.
   */
  Scheduled schedule(Duration delay, Runnable callback);

  /**
   * Schedules a runnable at a fixed rate on the context.
   *
   * @param callback The callback to schedule.
   */
  Scheduled schedule(Duration initialDelay, Duration interval, Runnable callback);

  /**
   * Closes the context.
   */
  @Override
  void close();

}
