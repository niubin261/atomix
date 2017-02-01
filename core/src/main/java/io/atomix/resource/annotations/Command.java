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
package io.atomix.resource.annotations;

import io.atomix.resource.Operation;

/**
 * Resource command annotation.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public @interface Command {

  /**
   * The command name.
   */
  String name();

  /**
   * The command type.
   */
  Class<? extends Operation> type();

  /**
   * The command path.
   */
  String path() default "";

  /**
   * The command method.
   */
  Method[] method() default Method.POST;

  /**
   * Command method.
   */
  enum Method {
    PUT,
    POST,
    DELETE,
  }
}
