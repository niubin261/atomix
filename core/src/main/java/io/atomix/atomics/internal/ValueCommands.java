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
 * limitations under the License.
 */
package io.atomix.atomics.internal;

import io.atomix.resource.Operation;

/**
 * Distributed value commands.
 * <p>
 * This class reserves serializable type IDs {@code 50} through {@code 59}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ValueCommands {

  private ValueCommands() {
  }

  /**
   * Abstract value command.
   */
  public static abstract class ValueCommand implements Operation {
    protected long ttl;

    protected ValueCommand() {
    }

    protected ValueCommand(long ttl) {
      this.ttl = ttl;
    }

    /**
     * Returns the time to live in milliseconds.
     *
     * @return The time to live in milliseconds.
     */
    public long ttl() {
      return ttl;
    }
  }

  /**
   * Abstract value query.
   */
  public static abstract class ValueQuery implements Operation {
    protected ValueQuery() {
    }
  }

  /**
   * Get query.
   */
  public static class Get extends ValueQuery {
    public Get() {
    }

    @Override
    public String getName() {
      return "get";
    }
  }

  /**
   * Set command.
   */
  public static class Set extends ValueCommand {
    private byte[] value;

    public Set() {
    }

    public Set(byte[] value) {
      this.value = value;
    }

    public Set(byte[] value, long ttl) {
      super(ttl);
      this.value = value;
    }

    @Override
    public String getName() {
      return "set";
    }

    public byte[] getValue() {
      return value;
    }

    public void setValue(byte[] value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.format("%s[value=%s]", getClass().getSimpleName(), value);
    }
  }

  /**
   * Compare and set command.
   */
  public static class CompareAndSet extends ValueCommand {
    private byte[] expect;
    private byte[] update;

    public CompareAndSet() {
    }

    public CompareAndSet(byte[] expect, byte[] update) {
      this.expect = expect;
      this.update = update;
    }

    public CompareAndSet(byte[] expect, byte[] update, long ttl) {
      super(ttl);
      this.expect = expect;
      this.update = update;
    }

    @Override
    public String getName() {
      return "compareAndSet";
    }

    public byte[] getExpect() {
      return expect;
    }

    public void setExpect(byte[] expect) {
      this.expect = expect;
    }

    public byte[] getUpdate() {
      return update;
    }

    public void setUpdate(byte[] update) {
      this.update = update;
    }

    @Override
    public String toString() {
      return String.format("%s[expect=%s, update=%s]", getClass().getSimpleName(), expect, update);
    }
  }

  /**
   * Get and set command.
   */
  public static class GetAndSet extends ValueCommand {
    private byte[] value;

    public GetAndSet() {
    }

    public GetAndSet(byte[] value) {
      this.value = value;
    }

    public GetAndSet(byte[] value, long ttl) {
      super(ttl);
      this.value = value;
    }

    @Override
    public String getName() {
      return "getAndSet";
    }

    public byte[] getValue() {
      return value;
    }

    public void setValue(byte[] value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.format("%s[value=%s]", getClass().getSimpleName(), value);
    }
  }

  /**
   * Register command.
   */
  public static class Register extends ValueCommand {
    @Override
    public String getName() {
      return "register";
    }
  }

  /**
   * Unregister command.
   */
  public static class Unregister extends ValueCommand {
    @Override
    public String getName() {
      return "unregister";
    }
  }
}
