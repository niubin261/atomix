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

/**
 * Resource event interface.
 * <p>
 * Resources should implement this interface for event messages sent from {@link ResourceStateMachine}s
 * to resource clients. Each event type should be associated with a unique {@link EventType} that indicates
 * the event identifier.
 */
public interface Event {
  /**
   * Returns the resource event type.
   * <p>
   * The event type should be unique to the specific event as it's used as an event identifier.
   *
   * @return The resource event type.
   */
  EventType type();
}
