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
package io.atomix.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Serializer.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class Serializer {
  private final ObjectMapper mapper;

  Serializer(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public byte[] writeValue(Object value) {
    try {
      return mapper.writeValueAsBytes(value);
    } catch (IOException e) {
      throw new SerializationException(e);
    }
  }

  public JsonNode readValue(byte[] value) {
    try {
      return mapper.readTree(value);
    } catch (IOException e) {
      throw new SerializationException(e);
    }
  }

  public <T> T readValue(byte[] value, Class<T> type) {
    try {
      return mapper.readValue(value, type) ;
    } catch (IOException e) {
      throw new SerializationException(e);
    }
  }

  public <T> T convertValue(Object value, Class<T> type) {
    try {
      return mapper.convertValue(value, type);
    } catch (Exception e) {
      throw new SerializationException(e);
    }
  }
}
