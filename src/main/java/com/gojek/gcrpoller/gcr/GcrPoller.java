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

import com.gojek.gcrpoller.PluginConfigurations;
import com.gojek.gcrpoller.message.CheckConnectionResultMessage;
import com.gojek.gcrpoller.message.CheckConnectionResultMessage.STATUS;
import com.gojek.gcrpoller.message.PackageMaterialProperties;
import com.gojek.gcrpoller.message.PackageMaterialProperty;
import com.gojek.gcrpoller.message.PackageRevisionMessage;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcrPoller {

  private static final String NO_IMAGE_FOUND = "";
  private final Logger logger = Logger.getLoggerFor(GcrPoller.class);
  private final GcrClient gcrClient;

  public GcrPoller(GcrClient gcrClient) {
    this.gcrClient = gcrClient;
  }

  public CheckConnectionResultMessage checkConnectionToRepository(
      PackageMaterialProperties repositoryConfiguration, String gcrToken) {

    String registryUrl =
        repositoryConfiguration.getProperty(PluginConfigurations.GCP_REGISTRY_URL).value();

    try {
      gcrClient.getCatalogAccessToken(registryUrl, gcrToken);

      return new CheckConnectionResultMessage(
          STATUS.SUCCESS, Collections.singletonList("Successfully connected to repository"));
    } catch (Exception e) {
      logger.error("Error checking connection to repository", e);
      return new CheckConnectionResultMessage(
          STATUS.FAILURE, Collections.singletonList(e.getMessage()));
    }
  }

  public CheckConnectionResultMessage checkConnectionToPackage(
      PackageMaterialProperties packageConfiguration,
      PackageMaterialProperties repositoryConfiguration,
      String gcrToken) {
    String project = repositoryConfiguration.getProperty(PluginConfigurations.GCP_PROJECT).value();
    String registryUrl =
        repositoryConfiguration.getProperty(PluginConfigurations.GCP_REGISTRY_URL).value();
    String imageName = packageConfiguration.getProperty(PluginConfigurations.DOCKER_IMAGE).value();

    try {
      Token tokenResponse =
          gcrClient.getImageAccessToken(registryUrl, project, imageName, gcrToken);
      gcrClient.getImageTags(registryUrl, project, imageName, tokenResponse.getToken());

      return new CheckConnectionResultMessage(
          STATUS.SUCCESS, Collections.singletonList("Successfully connect to repository"));
    } catch (Exception e) {
      logger.error("error checking connection to package", e);
      return new CheckConnectionResultMessage(
          STATUS.FAILURE, Collections.singletonList(e.getMessage()));
    }
  }

  public PackageRevisionMessage getLatestRevision(
      PackageMaterialProperties packageConfiguration,
      PackageMaterialProperties repositoryConfiguration,
      String gcrToken) {

    String project = repositoryConfiguration.getProperty(PluginConfigurations.GCP_PROJECT).value();
    String registryUrl =
        repositoryConfiguration.getProperty(PluginConfigurations.GCP_REGISTRY_URL).value();
    String imageName = packageConfiguration.getProperty(PluginConfigurations.DOCKER_IMAGE).value();

    Pattern pattern = getDockerTagPattern(packageConfiguration);

    try {
      Token tokenResponse =
          gcrClient.getImageAccessToken(registryUrl, project, imageName, gcrToken);
      ImageTags imageListResponse =
          gcrClient.getImageTags(registryUrl, project, imageName, tokenResponse.getToken());

      Collection<ImageManifest> images = imageListResponse.getManifest().values();
      ImageTag latestImageTag = getLatestImage(images, pattern);

      if (latestImageTag.getTag().equals(NO_IMAGE_FOUND)) {
        logger.debug("Unable to find latest image");
        return new PackageRevisionMessage();
      }

      Date date = new Date();
      date.setTime(latestImageTag.getTimestamp());
      return new PackageRevisionMessage(latestImageTag.getTag(), date, "", "", "");
    } catch (Exception e) {
      logger.error("error checking connection to package", e);
      return new PackageRevisionMessage();
    }
  }

  public PackageRevisionMessage getLatestRevisionSince(
      PackageMaterialProperties packageConfiguration,
      PackageMaterialProperties repositoryConfiguration,
      PackageRevisionMessage previous,
      String gcrToken) {

    String project = repositoryConfiguration.getProperty(PluginConfigurations.GCP_PROJECT).value();
    String registryUrl =
        repositoryConfiguration.getProperty(PluginConfigurations.GCP_REGISTRY_URL).value();
    String imageName = packageConfiguration.getProperty(PluginConfigurations.DOCKER_IMAGE).value();
    Pattern pattern = getDockerTagPattern(packageConfiguration);

    try {
      Token tokenResponse =
          gcrClient.getImageAccessToken(registryUrl, project, imageName, gcrToken);
      ImageTags imageListResponse =
          gcrClient.getImageTags(registryUrl, project, imageName, tokenResponse.getToken());

      Collection<ImageManifest> images = imageListResponse.getManifest().values();
      ImageTag latestImageTag = getLatestImage(images, pattern);

      ImageTag previousImageTag = imageFromPackageRevisionMessage(previous);
      if (latestImageTag.getTag().equals(NO_IMAGE_FOUND)
          || latestImageTag.equals(previousImageTag)) {
        logger.debug("Unable to find latest image");
        return new PackageRevisionMessage();
      }

      Date date = new Date();
      date.setTime(latestImageTag.getTimestamp());
      return new PackageRevisionMessage(latestImageTag.getTag(), date, "", "", "");
    } catch (Exception e) {
      logger.error("Error while getting latest revision since", e);
      return new PackageRevisionMessage();
    }
  }

  private Pattern getDockerTagPattern(PackageMaterialProperties packageConfiguration) {
    PackageMaterialProperty tagFilterProp =
        packageConfiguration.getProperty(PluginConfigurations.DOCKER_TAG_FILTER);
    String filter;
    if (tagFilterProp == null) {
      filter = "";
    } else {
      filter = tagFilterProp.value();
    }

    if (Strings.isNullOrEmpty(filter)) {
      filter = ".*";
    }

    return Pattern.compile(filter);
  }

  private ImageTag imageFromPackageRevisionMessage(PackageRevisionMessage previous) {
    long timestamp = previous.getTimestamp().toInstant().toEpochMilli();
    return new ImageTag(previous.getRevision(), timestamp);
  }

  private ImageTag getLatestImage(Collection<ImageManifest> imageManifests, Pattern pattern) {
    ImageTag latestImage = new ImageTag(NO_IMAGE_FOUND, 0);

    for (ImageManifest imageManifest : imageManifests) {
      boolean matchPattern = false;
      String matchTag = NO_IMAGE_FOUND;
      for (String tag : imageManifest.getTag()) {
        Matcher matcher = pattern.matcher(tag);
        if (matcher.find()) {
          matchPattern = true;
          matchTag = tag;
          break;
        }
      }

      if (!matchPattern) {
        continue;
      }

      long timeUploaded = Long.parseLong(imageManifest.getTimeUploadedMs());
      if (timeUploaded > latestImage.getTimestamp()) {
        latestImage = new ImageTag(matchTag, timeUploaded);
      }
    }

    return latestImage;
  }
}
