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

package com.gojek.gcrpoller.gcr;

import com.gojek.gcrpoller.exception.InvalidCredentialException;
import com.gojek.gcrpoller.exception.PollerException;
import com.gojek.gcrpoller.util.GsonObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class GcrClient {
  private static final Logger logger = Logger.getLoggerFor(GcrClient.class);

  private static final String GET_CATALOG_TOKEN_PATH =
      "http://%s/v2/token?service=gcr.io&service={registry_url}&scope=registry:catalog:*";
  private static final String GET_TOKEN_PATH =
      "http://%s/v2/token?service={registry_url}&scope=repository:{project}/{image_name}:pull";
  private static final String GET_IMAGE_LIST_PATH = "http://%s/v2/{project}/{image_name}/tags/list";

  public GcrClient() {
    Unirest.setObjectMapper(new GsonObjectMapper());
  }

  public Token getCatalogAccessToken(String registryUrl, String token) {
    HttpResponse<Token> response;
    try {
      response =
          Unirest.get(String.format(GET_CATALOG_TOKEN_PATH, registryUrl))
              .header("Authorization", formatBearerToken(token))
              .routeParam("registry_url", registryUrl)
              .asObject(Token.class);
      if (!isSuccess(response.getStatus())) {
        throw new InvalidCredentialException(
            "Invalid status code while getting GCR token = " + response.getStatus());
      }

    } catch (UnirestException e) {
      logger.error("Unable to get GCR token", e);
      throw new InvalidCredentialException("Unable to get GCR token");
    }

    return response.getBody();
  }

  public Token getImageAccessToken(
      String registryUrl, String project, String imageName, String token) {
    try {
      HttpResponse<Token> response =
          Unirest.get(String.format(GET_TOKEN_PATH, registryUrl))
              .header("Authorization", formatBearerToken(token))
              .routeParam("registry_url", registryUrl)
              .routeParam("project", project)
              .routeParam("image_name", imageName)
              .asObject(Token.class);

      if (!isSuccess(response.getStatus())) {
        throw new InvalidCredentialException(
            "Invalid status code while getting GCR token = " + response.getStatus());
      }

      return response.getBody();
    } catch (UnirestException e) {
      logger.error("Unable to get GCR token", e);
      throw new InvalidCredentialException("Unable to get GCR token");
    }
  }

  public ImageTags getImageTags(
      String registryUrl, String project, String imageName, String token) {
    HttpResponse<ImageTags> response;
    try {
      response =
          Unirest.get(String.format(GET_IMAGE_LIST_PATH, registryUrl))
              .header("Authorization", formatBearerToken(token))
              .routeParam("project", project)
              .routeParam("image_name", imageName)
              .asObject(ImageTags.class);
      if (!isSuccess(response.getStatus())) {
        throw new PollerException(
            "Invalid status code while getting image list = " + response.getStatus());
      }
      return response.getBody();
    } catch (UnirestException e) {
      logger.error("Unable to get image list {}", registryUrl, e);
      throw new PollerException("Unable to get image list");
    }
  }

  private String formatBearerToken(String token) {
    return String.format("Bearer %s", token);
  }

  private boolean isSuccess(int status) {
    return status >= 200 && status < 300;
  }
}
