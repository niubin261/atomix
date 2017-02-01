/*
 * Copyright 2015 the original author or authors.
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

import io.atomix.resource.Operation;

/**
 * Long commands.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public final class LongCommands {

  private LongCommands() {
  }

  /**
   * Abstract long command.
   */
  public static abstract class LongCommand<V> implements Operation {
    protected LongCommand() {
    }
  }

  /**
   * Increment and get command.
   */
  public static class IncrementAndGet extends LongCommand {
    @Override
    public String getName() {
      return "incrementAndGet";
    }
  }

  /**
   * Decrement and get command.
   */
  public static class DecrementAndGet extends LongCommand<Long> {
    @Override
    public String getName() {
      return "decrementAndGet";
    }
  }

  /**
   * Get and increment command.
   */
  public static class GetAndIncrement extends LongCommand<Long> {
    @Override
    public String getName() {
      return "getAndIncrement";
    }
  }

  /**
   * Get and decrement command.
   */
  public static class GetAndDecrement extends LongCommand<Long> {
    @Override
    public String getName() {
      return "getAndDecrement";
    }
  }

  /**
   * Delta command.
   */
  public static abstract class DeltaCommand extends LongCommand<Long> {
    private byte[] delta;

    public DeltaCommand() {
    }

    public DeltaCommand(byte[] delta) {
      this.delta = delta;
    }

    public byte[] getDelta() {
      return delta;
    }

    public void setDelta(byte[] delta) {
      this.delta = delta;
    }
  }

  /**
   * Get and add command.
   */
  public static class GetAndAdd extends DeltaCommand {
    public GetAndAdd() {
    }

    public GetAndAdd(byte[] delta) {
      super(delta);
    }

    @Override
    public String getName() {
      return "getAndAdd";
    }
  }

  /**
   * Add and get command.
   */
  public static class AddAndGet extends DeltaCommand {
    public AddAndGet() {
    }

    public AddAndGet(byte[] delta) {
      super(delta);
    }

    @Override
    public String getName() {
      return "addAndGet";
    }
  }
}
