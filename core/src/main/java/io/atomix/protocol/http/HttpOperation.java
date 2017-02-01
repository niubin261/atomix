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
package io.atomix.protocol.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * HTTP command.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class HttpOperation {
  private final String path;
  private final String method;
  private final Map<String, String> headers;
  private final String body;

  @JsonCreator
  public HttpOperation(
    @JsonProperty("path") String path,
    @JsonProperty("method") String method,
    @JsonProperty("headers") Map<String, String> headers,
    @JsonProperty("body") String body) {
    this.path = path;
    this.method = method;
    this.headers = headers;
    this.body = body;
  }

  /**
   * Returns the HTTP request path.
   *
   * @return The HTTP request path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the HTTP request method.
   *
   * @return The HTTP request method.
   */
  public String getMethod() {
    return method;
  }

  /**
   * Returns the HTTP request headers.
   *
   * @return The HTTP request headers.
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Returns the HTTP request body.
   *
   * @return The HTTP request body.
   */
  public String getBody() {
    return body;
  }
}
