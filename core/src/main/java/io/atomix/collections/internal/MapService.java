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

import io.atomix.copycat.util.concurrent.Scheduled;
import io.atomix.resource.Commit;
import io.atomix.resource.ResourceService;
import io.atomix.resource.Session;
import io.atomix.resource.annotations.Command;
import io.atomix.resource.annotations.Query;

import java.time.Duration;
import java.util.*;

import static io.atomix.collections.DistributedMap.EntryEvent;
import static io.atomix.collections.DistributedMap.Events;

/**
 * Map state machine.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class MapService extends ResourceService {
  private final Map<byte[], Value> map = new HashMap<>();
  private final Map<byte[], Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>>> listeners = new HashMap<>();

  @Override
  public void close(Session session) {
    // Remove the session from event listeners.
    Iterator<Map.Entry<byte[], Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>>>> keyIterator = listeners.entrySet().iterator();
    while (keyIterator.hasNext()) {
      Map.Entry<byte[], Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>>> keyEntry = keyIterator.next();
      Iterator<Map.Entry<Integer, Map<Long, Commit<MapCommands.KeyListen>>>> eventIterator = keyEntry.getValue().entrySet().iterator();
      while (eventIterator.hasNext()) {
        Map.Entry<Integer, Map<Long, Commit<MapCommands.KeyListen>>> eventEntry = eventIterator.next();
        Map<Long, Commit<MapCommands.KeyListen>> sessions = eventEntry.getValue();
        Commit<MapCommands.KeyListen> commit = sessions.remove(session.id());
        if (commit != null) {
          commit.compact(Commit.CompactionMode.QUORUM);
          if (sessions.isEmpty()) {
            eventIterator.remove();
          }
        }
      }
      if (keyEntry.getValue().isEmpty()) {
        keyIterator.remove();
      }
    }
  }

  /**
   * Notifies clients of an entry event.
   *
   * @param event The entry event.
   */
  protected void notify(EntryEvent event) {
    Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>> keyListeners = listeners.get(event.entry().getKey());
    if (keyListeners != null) {
      Map<Long, Commit<MapCommands.KeyListen>> eventListeners = keyListeners.get(event.type().id());
      if (eventListeners != null) {
        for (Commit<MapCommands.KeyListen> listener : eventListeners.values()) {
          listener.session().publish("key", event);
        }
      }
    }
    super.notify(event);
  }

  /**
   * Registers a key change listener.
   */
  @Command(name = "listen", type = MapCommands.KeyListen.class, path = "/events", method = Command.Method.POST)
  public void listen(Commit<MapCommands.KeyListen> commit) {
    Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>> listeners = this.listeners.computeIfAbsent(commit.operation().getKey(), k -> new HashMap<>());
    Map<Long, Commit<MapCommands.KeyListen>> sessions = listeners.computeIfAbsent(commit.operation().getEvent(), e -> new HashMap<>());
    if (!sessions.containsKey(commit.session().id())) {
      sessions.put(commit.session().id(), commit);
    } else {
      commit.compact(Commit.CompactionMode.QUORUM);
    }
  }

  /**
   * Unregisters a key change listener.
   */
  @Command(name = "unlisten", type = MapCommands.KeyUnlisten.class, path = "/events", method = Command.Method.DELETE)
  public void unlisten(Commit<MapCommands.KeyUnlisten> commit) {
    try {
      Map<Integer, Map<Long, Commit<MapCommands.KeyListen>>> listeners = this.listeners.get(commit.operation().getKey());
      if (listeners != null) {
        Map<Long, Commit<MapCommands.KeyListen>> sessions = listeners.get(commit.operation().getEvent());
        if (sessions != null) {
          Commit<MapCommands.KeyListen> listen = sessions.remove(commit.session().id());
          if (listen != null) {
            listen.compact(Commit.CompactionMode.QUORUM);
            if (sessions.isEmpty()) {
              listeners.remove(commit.operation().getEvent());
              if (listeners.isEmpty()) {
                this.listeners.remove(commit.operation().getKey());
              }
            }
          }
        }
      }
    } finally {
      commit.compact(Commit.CompactionMode.TOMBSTONE);
    }
  }

  /**
   * Handles a contains key commit.
   */
  @Query(name = "containsKey", type = MapCommands.ContainsKey.class, path = "/{key}/exists", method = Query.Method.GET)
  public boolean containsKey(Commit<MapCommands.ContainsKey> commit) {
    try {
      return map.containsKey(commit.operation().getKey());
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a contains value commit.
   */
  @Query(name = "containsValue", type = MapCommands.ContainsValue.class, path = "/containsValue", method = Query.Method.GET)
  public boolean containsValue(Commit<MapCommands.ContainsValue> commit) {
    try {
      for (Value value : map.values()) {
        if (Arrays.equals(value.commit.operation().getValue(), commit.operation().getValue())) {
          return true;
        }
      }
      return false;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a get commit.
   */
  @Query(name = "get", type = MapCommands.Get.class, path = "/{key}", method = Query.Method.GET)
  public byte[] get(Commit<MapCommands.Get> commit) {
    try {
      Value value = map.get(commit.operation().getKey());
      return value != null ? value.commit.operation().getValue() : null;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a get or default commit.
   */
  @Query(name = "getOrDefault", type = MapCommands.GetOrDefault.class)
  public byte[] getOrDefault(Commit<MapCommands.GetOrDefault> commit) {
    try {
      Value value = map.get(commit.operation().getKey());
      return value != null ? value.commit.operation().getValue() : commit.operation().getDefaultValue();
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a put commit.
   */
  @Command(name = "put", type = MapCommands.Put.class, path = "/{key}", method = Command.Method.PUT)
  public Object put(Commit<MapCommands.Put> commit) {
    try {
      Scheduled timer = commit.operation().getTtl() > 0 ? context.schedule(Duration.ofMillis(commit.operation().getTtl()), () -> {
        map.remove(commit.operation().getKey()).commit.close();
      }) : null;

      Value value = map.put(commit.operation().getKey(), new Value(commit, timer));
      if (value != null) {
        try {
          if (value.timer != null)
            value.timer.cancel();
          notify(new EntryEvent<>(Events.UPDATE, new MapEntry<>(commit.operation().getKey(), commit.operation().getValue())));
          return value.commit.operation().getValue();
        } finally {
          value.commit.close();
        }
      } else {
        notify(new EntryEvent<>(Events.ADD, new MapEntry<>(commit.operation().getKey(), commit.operation().getValue())));
      }
      return null;
    } catch (Exception e) {
      commit.close();
      throw e;
    }
  }

  /**
   * Handles a put if absent commit.
   */
  @Command(name = "putIfAbsent", type = MapCommands.PutIfAbsent.class)
  public Object putIfAbsent(Commit<MapCommands.PutIfAbsent> commit) {
    try {
      Value value = map.get(commit.operation().getKey());
      if (value == null) {
        Scheduled timer = commit.operation().getTtl() > 0 ? context.schedule(Duration.ofMillis(commit.operation().getTtl()), () -> {
          map.remove(commit.operation().getKey()).commit.close();
        }) : null;

        map.put(commit.operation().getKey(), new Value(commit, timer));
        notify(new EntryEvent<>(Events.ADD, new MapEntry<>(commit.operation().getKey(), commit.operation().getValue())));
        return null;
      } else {
        commit.close();
        return value.commit.operation().getValue();
      }
    } catch (Exception e) {
      commit.close();
      throw e;
    }
  }

  /**
   * Handles a remove commit.
   */
  @Command(name = "remove", type = MapCommands.Remove.class, path = "/{key}", method = Command.Method.DELETE)
  public Object remove(Commit<MapCommands.Remove> commit) {
    try {
      Value value = map.remove(commit.operation().getKey());
      if (value != null) {
        try {
          if (value.timer != null)
            value.timer.cancel();
          notify(new EntryEvent<>(Events.REMOVE, new MapEntry<>(value.commit.operation().getKey(), value.commit.operation().getValue())));
          return value.commit.operation().getValue();
        } finally {
          value.commit.close();
        }
      }
      return null;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a remove if present commit.
   */
  @Command(name = "removeIfPresent", type = MapCommands.RemoveIfPresent.class)
  public boolean removeIfPresent(Commit<MapCommands.RemoveIfPresent> commit) {
    try {
      Value value = map.get(commit.operation().getKey());
      if (value == null || ((value.commit.operation().getValue() == null && commit.operation().getValue() != null)
        || (value.commit.operation().getValue() != null && !Arrays.equals(value.commit.operation().getValue(), commit.operation().getValue())))) {
        return false;
      } else {
        try {
          map.remove(commit.operation().getKey());
          if (value.timer != null)
            value.timer.cancel();
          notify(new EntryEvent<>(Events.REMOVE, new MapEntry<>(value.commit.operation().getKey(), value.commit.operation().getValue())));
          return true;
        } finally {
          value.commit.close();
        }
      }
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a replace commit.
   */
  @Command(name = "replace", type = MapCommands.Replace.class)
  public Object replace(Commit<MapCommands.Replace> commit) {
    Value value = map.get(commit.operation().getKey());
    if (value != null) {
      try {
        if (value.timer != null)
          value.timer.cancel();
        Scheduled timer = commit.operation().getTtl() > 0 ? context.schedule(Duration.ofMillis(commit.operation().getTtl()), () -> {
          map.remove(commit.operation().getKey());
          commit.close();
        }) : null;
        map.put(commit.operation().getKey(), new Value(commit, timer));
        notify(new EntryEvent<>(Events.UPDATE, new MapEntry<>(commit.operation().getKey(), commit.operation().getValue())));
        return value.commit.operation().getValue();
      } finally {
        value.commit.close();
      }
    } else {
      commit.close();
    }
    return null;
  }

  /**
   * Handles a replace if present commit.
   */
  @Command(name = "replaceIfPresent", type = MapCommands.ReplaceIfPresent.class)
  public boolean replaceIfPresent(Commit<MapCommands.ReplaceIfPresent> commit) {
    Value value = map.get(commit.operation().getKey());
    if (value == null) {
      commit.close();
      return false;
    }

    if ((value.commit.operation().getValue() == null && commit.operation().getReplace() == null)
      || (value.commit.operation().getValue() != null && Arrays.equals(value.commit.operation().getValue(), commit.operation().getReplace()))) {
      if (value.timer != null)
        value.timer.cancel();
      Scheduled timer = commit.operation().getTtl() > 0 ? context.schedule(Duration.ofMillis(commit.operation().getTtl()), () -> {
        map.remove(commit.operation().getKey()).commit.close();
      }) : null;
      map.put(commit.operation().getKey(), new Value(commit, timer));
      notify(new EntryEvent<>(Events.UPDATE, new MapEntry<>(commit.operation().getKey(), commit.operation().getValue())));
      value.commit.close();
      return true;
    } else {
      commit.close();
    }
    return false;
  }

  /**
   * Handles a values query.
   */
  @Query(name = "values", type = MapCommands.Values.class)
  public Collection<Object> values(Commit<MapCommands.Values> commit) {
    try {
      Collection<Object> values = new ArrayList<>();
      for (Value value : map.values()) {
        values.add(value.commit.operation().getValue());
      }
      return values;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a key set query.
   */
  @Query(name = "keySet", type = MapCommands.KeySet.class)
  public Set<Object> keySet(Commit<MapCommands.KeySet> commit) {
    try {
      return new HashSet<>(map.keySet());
    } finally {
      commit.close();
    }
  }

  /**
   * Handles an entry set query.
   */
  @Query(name = "entrySet", type = MapCommands.EntrySet.class)
  public Set<Map.Entry<Object, Object>> entrySet(Commit<MapCommands.EntrySet> commit) {
    try {
      Set<Map.Entry<Object, Object>> entries = new HashSet<>();
      for (Map.Entry<byte[], Value> entry : map.entrySet()) {
        entries.add(new MapEntry<>(entry.getKey(), entry.getValue().commit.operation().getValue()));
      }
      return entries;
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a count commit.
   */
  @Query(name = "size", type = MapCommands.Size.class)
  public int size(Commit<MapCommands.Size> commit) {
    try {
      return map.size();
    } finally {
      commit.close();
    }
  }

  /**
   * Handles an is empty commit.
   */
  @Query(name = "isEmpty", type = MapCommands.IsEmpty.class)
  public boolean isEmpty(Commit<MapCommands.IsEmpty> commit) {
    try {
      return map == null || map.isEmpty();
    } finally {
      commit.close();
    }
  }

  /**
   * Handles a clear commit.
   */
  @Command(name = "clear", type = MapCommands.Clear.class, path = "/", method = Command.Method.DELETE)
  public void clear(Commit<MapCommands.Clear> commit) {
    try {
      destroy();
    } finally {
      commit.close();
    }
  }

  @Override
  public void destroy() {
    Iterator<Map.Entry<byte[], Value>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<byte[], Value> entry = iterator.next();
      Value value = entry.getValue();
      if (value.timer != null)
        value.timer.cancel();
      value.commit.close();
      iterator.remove();
    }
  }

  /**
   * Map value.
   */
  private static class Value {
    private final Commit<? extends MapCommands.TtlOperation> commit;
    private final Scheduled timer;

    private Value(Commit<? extends MapCommands.TtlOperation> commit, Scheduled timer) {
      this.commit = commit;
      this.timer = timer;
    }
  }

}
