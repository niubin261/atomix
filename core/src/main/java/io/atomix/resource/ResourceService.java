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
 * Resource service.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class ResourceService {
  protected ResourceContext context;

  public void register(Session session) {

  }

  public void unregister(Session session) {

  }

  public void expire(Session session) {

  }

  public void close(Session session) {

  }

  /**
   * Initializes the resource.
   *
   * @param context The resource context.
   */
  public void init(ResourceContext context) {
    this.context = context;
  }

  /**
   * Notifies listeners of an event.
   *
   * @param event The event for which to notify listeners.
   */
  protected void notify(Event event) {
  }

  /**
   * Destroys the resource state.
   */
  public void destroy() {
  }
}
