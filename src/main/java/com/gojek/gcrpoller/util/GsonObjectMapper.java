/*
 *    Copyright 2019 GOJEK
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.gojek.gcrpoller.util;

import com.google.gson.Gson;
import com.mashape.unirest.http.ObjectMapper;

public class GsonObjectMapper implements ObjectMapper {
  private Gson gson = new Gson();

  public <T> T readValue(String s, Class<T> aClass) {
    try {
      return gson.fromJson(s, aClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String writeValue(Object o) {
    try {
      return gson.toJson(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
