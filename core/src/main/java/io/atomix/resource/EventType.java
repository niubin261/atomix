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
 * Resource event type.
 * <p>
 * An event type should be created for each distinct {@link Event} supported by a resource type.
 * The event type provides an {@link #id()} that is used internally by Atomix to invoke event
 * handlers within the resource client.
 */
public interface EventType {
  /**
   * Returns the event type ID.
   * <p>
   * The event type ID is used to identify event types when published from the server state
   * machine to the client. Event type IDs must be unique within a resource type.
   *
   * @return The event type ID. This must be unique within a resource type.
   */
  int id();
}
