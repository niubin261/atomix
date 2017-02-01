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
package io.atomix.atomics.internal;

import io.atomix.copycat.util.concurrent.Scheduled;
import io.atomix.resource.annotations.Command;
import io.atomix.resource.Commit;
import io.atomix.resource.annotations.Query;
import io.atomix.resource.ResourceService;

import java.time.Duration;
import java.util.Arrays;

/**
 * Abstract distributed value state machine.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AbstractValueService extends ResourceService {
  protected byte[] value;
  protected Commit<? extends ValueCommands.ValueCommand> current;
  protected Scheduled timer;

  /**
   * Handles a get commit.
   */
  @Query(name = "get", path = "/", method = Query.Method.GET)
  public byte[] get(Commit<ValueCommands.Get> commit) {
    try {
      return current != null ? value : null;
    } finally {
      commit.close();
    }
  }

  /**
   * Cleans the current commit.
   */
  private void cleanCurrent() {
    if (current != null) {
      if (timer != null) {
        timer.cancel();
        timer = null;
      }
      current.close();
    }
  }

  /**
   * Sets the current commit.
   */
  private void setCurrent(Commit<? extends ValueCommands.ValueCommand> commit) {
    timer = commit.operation().ttl() > 0 ? context.schedule(Duration.ofMillis(commit.operation().ttl()), () -> {
      value = null;
      current.close();
      current = null;
    }) : null;
    current = commit;
  }

  /**
   * Applies a set commit.
   */
  @Command(name = "set", path = "/", method = {Command.Method.POST, Command.Method.PUT})
  public void set(Commit<ValueCommands.Set> commit) {
    cleanCurrent();
    value = commit.operation().getValue();
    setCurrent(commit);
  }

  /**
   * Handles a compare and set commit.
   */
  @Command(name = "compareAndSet", path = "/compareAndSet", method = Command.Method.PUT)
  public boolean compareAndSet(Commit<ValueCommands.CompareAndSet> commit) {
    if ((value == null && commit.operation().getExpect() == null) || (value != null && commit.operation().getExpect() != null && Arrays.equals(value, commit.operation().getExpect()))) {
      value = commit.operation().getUpdate();
      cleanCurrent();
      setCurrent(commit);
      return true;
    } else {
      commit.close();
      return false;
    }
  }

  /**
   * Handles a get and set commit.
   */
  @Command(name = "getAndSet", path = "/getAndSet", method = Command.Method.PUT)
  public byte[] getAndSet(Commit<ValueCommands.GetAndSet> commit) {
    byte[] result = value;
    value = commit.operation().getValue();
    cleanCurrent();
    setCurrent(commit);
    return result;
  }

  @Override
  public void destroy() {
    if (current != null) {
      current.close();
      current = null;
      value = null;
    }
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }
}
