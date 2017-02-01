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
package io.atomix.collections.internal;

import io.atomix.collections.DistributedMap;
import io.atomix.copycat.util.Assert;
import io.atomix.resource.Operation;

import java.util.Collection;
import java.util.Set;

/**
 * Map commands.
 * <p>
 * This class reserves serializable type IDs {@code 60} through {@code 74}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class MapCommands {

  private MapCommands() {
  }

  /**
   * Abstract map command.
   */
  public static abstract class MapOperation implements Operation {
  }

  /**
   * Abstract key-based command.
   */
  public static abstract class KeyOperation extends MapOperation {
    protected byte[] key;

    public KeyOperation() {
    }

    public KeyOperation(byte[] key) {
      this.key = Assert.notNull(key, "key");
    }

    public byte[] getKey() {
      return key;
    }

    public void setKey(byte[] key) {
      this.key = key;
    }
  }

  /**
   * Contains key command.
   */
  public static class ContainsKey extends KeyOperation {
    public ContainsKey() {
    }

    public ContainsKey(byte[] key) {
      super(key);
    }

    @Override
    public String getName() {
      return "containsKey";
    }
  }

  /**
   * Abstract key-based query.
   */
  public static class ContainsValue extends MapOperation {
    protected byte[] value;

    public ContainsValue() {
    }

    public ContainsValue(byte[] value) {
      this.value = Assert.notNull(value, "value");
    }

    public byte[] getValue() {
      return value;
    }

    public void setValue(byte[] value) {
      this.value = value;
    }

    @Override
    public String getName() {
      return "containsValue";
    }
  }

  /**
   * Key/value command.
   */
  public static abstract class KeyValueOperation extends KeyOperation {
    protected byte[] value;

    public KeyValueOperation() {
    }

    public KeyValueOperation(byte[] key, byte[] value) {
      super(key);
      this.value = value;
    }

    public byte[] getValue() {
      return value;
    }

    public void setValue(byte[] value) {
      this.value = value;
    }
  }

  /**
   * TTL command.
   */
  public static abstract class TtlOperation extends KeyValueOperation {
    protected long ttl;

    public TtlOperation() {
    }

    public TtlOperation(byte[] key, byte[] value, long ttl) {
      super(key, value);
      this.ttl = ttl;
    }

    public long getTtl() {
      return ttl;
    }

    public void setTtl(long ttl) {
      this.ttl = ttl;
    }
  }

  /**
   * Put command.
   */
  public static class Put extends TtlOperation {
    public Put() {
    }

    public Put(byte[] key, byte[] value) {
      this(key, value, 0);
    }

    public Put(byte[] key, byte[] value, long ttl) {
      super(key, value, ttl);
    }

    @Override
    public String getName() {
      return "put";
    }
  }

  /**
   * Put if absent command.
   */
  public static class PutIfAbsent extends TtlOperation {
    public PutIfAbsent() {
    }

    public PutIfAbsent(byte[] key, byte[] value) {
      this(key, value, 0);
    }

    public PutIfAbsent(byte[] key, byte[] value, long ttl) {
      super(key, value, ttl);
    }

    @Override
    public String getName() {
      return "putIfAbsent";
    }
  }

  /**
   * Get query.
   */
  public static class Get extends KeyOperation {
    public Get() {
    }

    public Get(byte[] key) {
      super(key);
    }

    @Override
    public String getName() {
      return "get";
    }
  }

  /**
   * Get or default query.
   */
  public static class GetOrDefault extends KeyOperation {
    private byte[] defaultValue;

    public GetOrDefault() {
    }

    public GetOrDefault(byte[] key, byte[] defaultValue) {
      super(key);
      this.defaultValue = defaultValue;
    }

    public byte[] getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(byte[] defaultValue) {
      this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
      return "getOrDefault";
    }
  }

  /**
   * Remove command.
   */
  public static class Remove extends KeyOperation {

    public Remove() {
    }

    public Remove(byte[] key) {
      super(key);
    }

    @Override
    public String getName() {
      return "remove";
    }
  }

  /**
   * Remove if absent command.
   */
  public static class RemoveIfPresent extends KeyValueOperation {

    public RemoveIfPresent() {
    }

    public RemoveIfPresent(byte[] key, byte[] value) {
      super(key, value);
    }

    @Override
    public String getName() {
      return "removeIfPresent";
    }
  }

  /**
   * Remove command.
   */
  public static class Replace extends TtlOperation {
    public Replace() {
    }

    public Replace(byte[] key, byte[] value) {
      this(key, value, 0);
    }

    public Replace(byte[] key, byte[] value, long ttl) {
      super(key, value, ttl);
    }

    @Override
    public String getName() {
      return "replace";
    }
  }

  /**
   * Remove if absent command.
   */
  public static class ReplaceIfPresent extends TtlOperation {
    private byte[] replace;

    public ReplaceIfPresent() {
    }

    public ReplaceIfPresent(byte[] key, byte[] replace, byte[] value) {
      this(key, replace, value, 0);
    }

    public ReplaceIfPresent(byte[] key, byte[] replace, byte[] value, long ttl) {
      super(key, value, ttl);
      this.replace = replace;
    }

    public byte[] getReplace() {
      return replace;
    }

    public void setReplace(byte[] replace) {
      this.replace = replace;
    }

    @Override
    public String getName() {
      return "replaceIfPresent";
    }
  }

  /**
   * Is empty query.
   */
  public static class IsEmpty extends MapOperation {
    @Override
    public String getName() {
      return "isEmpty";
    }
  }

  /**
   * Size query.
   */
  public static class Size extends MapOperation {
    @Override
    public String getName() {
      return "size";
    }
  }

  /**
   * Values query.
   */
  public static class Values extends MapOperation {
    @Override
    public String getName() {
      return "values";
    }
  }

  /**
   * Key set query.
   */
  public static class KeySet extends MapOperation {
    @Override
    public String getName() {
      return "keySet";
    }
  }

  /**
   * Entry set query.
   */
  public static class EntrySet extends MapOperation {
    @Override
    public String getName() {
      return "entrySet";
    }
  }

  /**
   * Clear command.
   */
  public static class Clear extends MapOperation {
    @Override
    public String getName() {
      return "clear";
    }
  }

  /**
   * Map key listen command.
   */
  public static abstract class EventCommand extends MapOperation {
    private int event;
    private byte[] key;

    protected EventCommand() {
    }

    protected EventCommand(int event, byte[] key) {
      this.event = event;
      this.key = key;
    }

    public int getEvent() {
      return event;
    }

    public void setEvent(int event) {
      this.event = event;
    }

    public byte[] getKey() {
      return key;
    }

    public void setKey(byte[] key) {
      this.key = key;
    }
  }

  /**
   * Map key listen command.
   */
  public static class KeyListen extends EventCommand {
    public KeyListen() {
    }

    public KeyListen(int event, byte[] key) {
      super(event, key);
    }

    @Override
    public String getName() {
      return "listen";
    }
  }

  /**
   * Map key unlisten command.
   */
  public static class KeyUnlisten extends EventCommand {
    public KeyUnlisten() {
    }

    public KeyUnlisten(int event, byte[] key) {
      super(event, key);
    }

    @Override
    public String getName() {
      return "unlisten";
    }
  }
}
