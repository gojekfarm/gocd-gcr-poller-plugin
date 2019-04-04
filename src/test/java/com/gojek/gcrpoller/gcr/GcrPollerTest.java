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

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gojek.gcrpoller.JsonUtil;
import com.gojek.gcrpoller.PluginConfigurations;
import com.gojek.gcrpoller.exception.InvalidCredentialException;
import com.gojek.gcrpoller.message.CheckConnectionResultMessage;
import com.gojek.gcrpoller.message.PackageMaterialProperties;
import com.gojek.gcrpoller.message.PackageMaterialProperty;
import com.gojek.gcrpoller.message.PackageRevisionMessage;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GcrPollerTest {

  private GcrPoller gcrPoller;
  private GcrClient gcrClient;

  @Before
  public void setUp() throws Exception {
    gcrClient = mock(GcrClient.class);
    gcrPoller = new GcrPoller(gcrClient);
  }

  @Test
  public void checkConnectionToRepositoryTest() {
    String registryUrl = "localhost";
    String gcrToken = "gcr_token";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    CheckConnectionResultMessage result =
        gcrPoller.checkConnectionToRepository(repoConfig, gcrToken);

    verify(gcrClient).getCatalogAccessToken(registryUrl, gcrToken);
    assertTrue(result.success());
  }

  @Test
  public void checkConnectionToRepositoryFailureCase() {
    String errorMessage = "invalid token";
    when(gcrClient.getCatalogAccessToken(anyString(), anyString()))
        .thenThrow(new InvalidCredentialException(errorMessage));

    String registryUrl = "localhost";
    String gcrToken = "gcr_token";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    CheckConnectionResultMessage result =
        gcrPoller.checkConnectionToRepository(repoConfig, gcrToken);

    assertFalse(result.success());
    assertThat(result.getMessages().get(0), equalTo(errorMessage));
  }

  @Test
  public void getLatestRevisionTest() throws IOException {
    URL resourceUrl = Resources.getResource("responses/get_image_tags_resp.json");
    String imageTagResp = Resources.toString(resourceUrl, Charsets.UTF_8);
    ImageTags imageTags = JsonUtil.fromJsonString(imageTagResp, ImageTags.class);
    String tokenValue = "my_token";
    Token token = new Token("1", "2", tokenValue);

    when(gcrClient.getImageAccessToken(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(token);
    when(gcrClient.getImageTags(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(imageTags);

    String gcrToken = "gcr_token";
    PackageMaterialProperties packageConfig = new PackageMaterialProperties();
    packageConfig.addPackageMaterialProperty(
        PluginConfigurations.DOCKER_IMAGE, new PackageMaterialProperty().withValue("myimage"));

    String project = "my-gcp-project";
    String registryUrl = "gcr.io";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue(project));
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    PackageRevisionMessage result =
        gcrPoller.getLatestRevision(packageConfig, repoConfig, gcrToken);

    Date date = new Date();
    date.setTime(4);
    assertThat(result.getRevision(), equalTo("2.1.0"));
    assertThat(result.getTimestamp(), equalTo(date));
  }

  @Test
  public void getLatestRevisionWithTagFilter() throws IOException {
    URL resourceUrl = Resources.getResource("responses/get_image_tags_resp.json");
    String imageTagResp = Resources.toString(resourceUrl, Charsets.UTF_8);
    ImageTags imageTags = JsonUtil.fromJsonString(imageTagResp, ImageTags.class);
    String tokenValue = "my_token";
    Token token = new Token("1", "2", tokenValue);

    when(gcrClient.getImageAccessToken(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(token);
    when(gcrClient.getImageTags(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(imageTags);

    String gcrToken = "gcr_token";
    PackageMaterialProperties packageConfig = new PackageMaterialProperties();
    packageConfig.addPackageMaterialProperty(
        PluginConfigurations.DOCKER_IMAGE, new PackageMaterialProperty().withValue("myimage"));
    packageConfig.addPackageMaterialProperty(
        PluginConfigurations.DOCKER_TAG_FILTER, new PackageMaterialProperty().withValue("^1.*"));

    String project = "my-gcp-project";
    String registryUrl = "gcr.io";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue(project));
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    PackageRevisionMessage result =
        gcrPoller.getLatestRevision(packageConfig, repoConfig, gcrToken);

    Date date = new Date();
    date.setTime(3);
    assertThat(result.getRevision(), equalTo("1.1.1"));
    assertThat(result.getTimestamp(), equalTo(date));
  }


  @Test
  public void getLatestRevisionSinceTest() throws IOException {
    URL resourceUrl = Resources.getResource("responses/get_image_tags_resp.json");
    String imageTagResp = Resources.toString(resourceUrl, Charsets.UTF_8);
    ImageTags imageTags = JsonUtil.fromJsonString(imageTagResp, ImageTags.class);
    String tokenValue = "my_token";
    Token token = new Token("1", "2", tokenValue);

    when(gcrClient.getImageAccessToken(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(token);
    when(gcrClient.getImageTags(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(imageTags);

    String gcrToken = "gcr_token";
    PackageMaterialProperties packageConfig = new PackageMaterialProperties();
    packageConfig.addPackageMaterialProperty(
        PluginConfigurations.DOCKER_IMAGE, new PackageMaterialProperty().withValue("myimage"));

    String project = "my-gcp-project";
    String registryUrl = "gcr.io";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue(project));
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    Date previousImageDate = new Date();
    previousImageDate.setTime(2);
    PackageRevisionMessage previous = new PackageRevisionMessage("1.1.0", previousImageDate, "", "", "");
    PackageRevisionMessage result =
        gcrPoller.getLatestRevisionSince(packageConfig, repoConfig, previous, gcrToken);

    Date latestImageDate = new Date();
    latestImageDate.setTime(4);
    assertThat(result.getRevision(), equalTo("2.1.0"));
    assertThat(result.getTimestamp(), equalTo(latestImageDate));
  }


  @Test
  public void getLatestRevisionSinceNoUpdate() throws IOException {
    URL resourceUrl = Resources.getResource("responses/get_image_tags_resp.json");
    String imageTagResp = Resources.toString(resourceUrl, Charsets.UTF_8);
    ImageTags imageTags = JsonUtil.fromJsonString(imageTagResp, ImageTags.class);
    String tokenValue = "my_token";
    Token token = new Token("1", "2", tokenValue);

    when(gcrClient.getImageAccessToken(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(token);
    when(gcrClient.getImageTags(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(imageTags);

    String gcrToken = "gcr_token";
    PackageMaterialProperties packageConfig = new PackageMaterialProperties();
    packageConfig.addPackageMaterialProperty(
        PluginConfigurations.DOCKER_IMAGE, new PackageMaterialProperty().withValue("myimage"));

    String project = "my-gcp-project";
    String registryUrl = "gcr.io";
    PackageMaterialProperties repoConfig = new PackageMaterialProperties();
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue(project));
    repoConfig.addPackageMaterialProperty(
        PluginConfigurations.GCP_REGISTRY_URL,
        new PackageMaterialProperty().withValue(registryUrl));

    Date previousImageDate = new Date();
    previousImageDate.setTime(4);
    PackageRevisionMessage previous = new PackageRevisionMessage("2.1.0", previousImageDate, "", "", "");
    PackageRevisionMessage result =
        gcrPoller.getLatestRevisionSince(packageConfig, repoConfig, previous, gcrToken);
    assertNull(result.getRevision());
    assertNull(result.getTimestamp());
  }
}
