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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.gojek.gcrpoller.JsonUtil;
import com.gojek.gcrpoller.exception.InvalidCredentialException;
import com.gojek.gcrpoller.exception.PollerException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mashape.unirest.http.utils.URLParamEncoder;
import java.io.IOException;
import java.net.URL;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GcrClientTest {

  private MockWebServer mockWebServer;
  private String url;
  private GcrClient gcrClient;

  @Before
  public void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    url = String.format("%s:%s", mockWebServer.getHostName(), mockWebServer.getPort());

    gcrClient = new GcrClient();
  }

  @Test
  public void getCatalogAccessTokenShouldSendValidRequest() throws InterruptedException {
    MockResponse mockGetCatalogAccessToken = new MockResponse();
    Token token = new Token("1234", "1234", "secret_token");
    mockGetCatalogAccessToken.setBody(JsonUtil.toJsonString(token));
    mockWebServer.enqueue(mockGetCatalogAccessToken);

    String gcrToken = "gcr_token";
    Token tkn = gcrClient.getCatalogAccessToken(url, gcrToken);
    assertThat(tkn, equalTo(token));

    assertThat(mockWebServer.getRequestCount(), equalTo(1));
    RecordedRequest recRequest = mockWebServer.takeRequest();

    String authHeader = recRequest.getHeader("Authorization");
    assertThat(authHeader, equalTo(String.format("Bearer %s", gcrToken)));
    assertThat(
        recRequest.getPath(),
        equalTo(
            String.format(
                "/v2/token?service=gcr.io&service=%s&scope=registry:catalog:*",
                URLParamEncoder.encode(url))));
  }

  @Test(expected = InvalidCredentialException.class)
  public void getCatalogAccessTokenShouldThrowInvalidCredentialExceptionIfResponseIsNot2XX() {
    MockResponse mockFailedResponse = new MockResponse();
    mockFailedResponse.setResponseCode(300);

    mockWebServer.enqueue(mockFailedResponse);
    String gcrToken = "gcr_token";

    gcrClient.getCatalogAccessToken(url, gcrToken);
  }


  @Test
  public void getImageAccessTokenShouldSendValidRequest() throws InterruptedException {
    MockResponse mockGetCatalogAccessToken = new MockResponse();
    Token gcrToken = new Token("1234", "1234", "secret_token");
    mockGetCatalogAccessToken.setBody(JsonUtil.toJsonString(gcrToken));
    mockWebServer.enqueue(mockGetCatalogAccessToken);

    String token = "my_token";
    String project = "gcp-project";
    String imageName = "myImage";
    Token tkn = gcrClient.getImageAccessToken(url, project, imageName, token);

    assertThat(tkn, equalTo(gcrToken));

    assertThat(mockWebServer.getRequestCount(), equalTo(1));
    RecordedRequest recRequest = mockWebServer.takeRequest();

    String authHeader = recRequest.getHeader("Authorization");
    assertThat(authHeader, equalTo(String.format("Bearer %s", token)));
    assertThat(
        recRequest.getPath(),
        equalTo(
            String.format(
                "/v2/token?service=%s&scope=repository:%s/%s:pull",
                URLParamEncoder.encode(url), project, imageName)));
  }

  @Test(expected = InvalidCredentialException.class)
  public void getImageAccessTokenShouldThrowInvalidCredentialExceptionIfResponseIsNot2XX() {
    MockResponse mockFailedResponse = new MockResponse();
    mockFailedResponse.setResponseCode(300);

    mockWebServer.enqueue(mockFailedResponse);
    String token = "my_token";
    String project = "gcp-project";
    String imageName = "myImage";
    gcrClient.getImageAccessToken(url, project, imageName, token);
  }

  @Test
  public void getImageTagsShouldSendValidRequest() throws IOException, InterruptedException {
    MockResponse mockGetImageTagsResponse = new MockResponse();

    URL resourceUrl = Resources.getResource("responses/get_image_tags_resp.json");
    String imageTagResp = Resources.toString(resourceUrl, Charsets.UTF_8);
    ImageTags ref = JsonUtil.fromJsonString(imageTagResp, ImageTags.class);

    mockGetImageTagsResponse.setBody(imageTagResp);

    mockWebServer.enqueue(mockGetImageTagsResponse);

    String token = "my_token";
    String project = "gcp-project";
    String imageName = "myImage";
    ImageTags imageTags = gcrClient.getImageTags(url, project, imageName, token);

    assertThat(imageTags, equalTo(ref));

    assertThat(mockWebServer.getRequestCount(), equalTo(1));
    RecordedRequest recRequest = mockWebServer.takeRequest();

    String authHeader = recRequest.getHeader("Authorization");
    assertThat(authHeader, equalTo(String.format("Bearer %s", token)));
    assertThat(
        recRequest.getPath(),
        equalTo(
            String.format(
                "/v2/%s/%s/tags/list",
                URLParamEncoder.encode(project), URLParamEncoder.encode(imageName))));
  }

  @Test(expected = PollerException.class)
  public void getImageTagsShouldThrowPollerExceptionIfResponseIsNot2XX() {
    MockResponse mockFailedResponse = new MockResponse();
    mockFailedResponse.setResponseCode(300);

    mockWebServer.enqueue(mockFailedResponse);
    String token = "my_token";
    String project = "gcp-project";
    String imageName = "myImage";
    gcrClient.getImageTags(url, project, imageName, token);
  }


  @After
  public void tearDown() throws Exception {
    mockWebServer.shutdown();
  }
}
