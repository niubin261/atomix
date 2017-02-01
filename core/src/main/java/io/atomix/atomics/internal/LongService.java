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

import io.atomix.atomics.DistributedLong;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.storage.buffer.HeapBuffer;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import io.atomix.resource.annotations.Command;
import io.atomix.resource.Commit;

/**
 * Long state machine.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class LongService extends AbstractValueService implements Snapshottable {
  private final HeapBuffer buffer = HeapBuffer.allocate(8);

  @Override
  public void snapshot(SnapshotWriter writer) {
    writer.write(value);
  }

  @Override
  public void install(SnapshotReader reader) {
    value = new byte[8];
    reader.read(value);
  }

  /**
   * Copies the given value to a byte array.
   */
  private byte[] copy(Long value) {
    if (value == null) {
      return new byte[0];
    }
    byte[] bytes = new byte[8];
    buffer.clear().writeLong(value).flip();
    buffer.read(bytes);
    return bytes;
  }

  /**
   * Reads the given value as a long.
   */
  private long read(byte[] value) {
    return buffer.clear().write(value).flip().readLong();
  }

  /**
   * Handles an increment and get commit.
   */
  @Command(name = "incrementAndGet", path = "incrementAndGet", method = Command.Method.PUT)
  public byte[] incrementAndGet(Commit<LongCommands.IncrementAndGet> commit) {
    try {
      if (value == null) {
        value = copy(1l);
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) + 1);
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
      }
      return value;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a decrement and get commit.
   */
  @Command(name = "decrementAndGet", path = "decrementAndGet", method = Command.Method.PUT)
  public byte[] decrementAndGet(Commit<LongCommands.DecrementAndGet> commit) {
    try {
      if (value == null) {
        value = copy(-1l);
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) - 1);
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
      }
      return value;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a get and increment commit.
   */
  @Command(name = "getAndIncrement", path = "getAndIncrement", method = Command.Method.PUT)
  public byte[] getAndIncrement(Commit<LongCommands.GetAndIncrement> commit) {
    try {
      if (value == null) {
        value = copy(1l);
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
        return new byte[0];
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) + 1);
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
        return oldValue;
      }
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a get and decrement commit.
   */
  @Command(name = "getAndDecrement", path = "getAndDecrement", method = Command.Method.PUT)
  public byte[] getAndDecrement(Commit<LongCommands.GetAndDecrement> commit) {
    try {
      if (value == null) {
        value = copy(-1l);
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
        return new byte[0];
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) - 1);
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
        return oldValue;
      }
    } finally {
      commit.close();
    }
  }

  /**
   * Handles an add and get commit.
   */
  @Command(name = "addAndGet", path = "addAndGet", method = Command.Method.PUT)
  public byte[] addAndGet(Commit<LongCommands.AddAndGet> commit) {
    try {
      if (value == null) {
        value = commit.operation().getDelta();
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
        return value;
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) + read(commit.operation().getDelta()));
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
        return value;
      }
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a get and add commit.
   */
  @Command(name = "getAndAdd", path = "getAndAdd", method = Command.Method.PUT)
  public byte[] getAndAdd(Commit<LongCommands.GetAndAdd> commit) {
    try {
      if (value == null) {
        value = commit.operation().getDelta();
        notify(new DistributedLong.ChangeEvent<>(new byte[0], value));
        return new byte[0];
      } else {
        byte[] oldValue = value;
        value = copy(read(oldValue) + read(commit.operation().getDelta()));
        notify(new DistributedLong.ChangeEvent<>(oldValue, value));
        return oldValue;
      }
    } finally {
      commit.close();
    }
  }
}
