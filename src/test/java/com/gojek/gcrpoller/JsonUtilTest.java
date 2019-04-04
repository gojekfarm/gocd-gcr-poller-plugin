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

package com.gojek.gcrpoller;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gojek.gcrpoller.message.LatestPackageRevisionSinceMessage;
import com.gojek.gcrpoller.message.PackageMaterialProperty;
import com.gojek.gcrpoller.message.PackageRevisionMessage;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class JsonUtilTest {

  @Test
  public void shouldConvertResponseToAppropriateJson() {
    PackageRevisionMessage response = new PackageRevisionMessage("test", Date.from(
        Instant.ofEpochMilli(0)), "none", "comment", "url");
    String actual = JsonUtil.toJsonString(response);
    String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(Date.from(Instant.ofEpochMilli(0)));
    String expected =
        String.format(
            "{\"revision\":\"test\",\"timestamp\":\"%s\",\"user\":\"none\",\"revisionComment\":\"comment\",\"trackbackUrl\":\"url\"}",
            timestamp);
    assertEquals(expected, actual);
  }

  @Test
  public void shouldUnmarshalRequestJsonCorrectly() {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .format(Date.from(Instant.ofEpochMilli(0)));
    String request = String.format("{\n"
        + "    \"repository-configuration\": {\n"
        + "        \"REPO_URL\": {\n"
        + "            \"value\": \"http://localhost.com\"\n"
        + "        }\n"
        + "    },\n"
        + "    \"package-configuration\": {\n"
        + "        \"PACKAGE_SPEC\": {\n"
        + "            \"value\": \"sample-package-1.0\"\n"
        + "        }\n"
        + "    },\n"
        + "    \"previous-revision\": {\n"
        + "        \"revision\": \"abc-10.2.1.rpm\",\n"
        + "        \"timestamp\": \"%s\",\n"
        + "        \"data\": {\n"
        + "            \"VERSION\": \"5.3.0\",\n"
        + "            \"LOCATION\": \"http://www.sample.org/location/of/package\"\n"
        + "        }\n"
        + "    }\n"
        + "}", timestamp);
    LatestPackageRevisionSinceMessage actual = JsonUtil
        .fromJsonString(request, LatestPackageRevisionSinceMessage.class);
    Map<String, PackageMaterialProperty>  repositoryConfig = new HashMap<>();
    repositoryConfig.put("REPO_URL", new PackageMaterialProperty().withValue("http://localhost.com"));
    Map<String, PackageMaterialProperty>  packageConfig = new HashMap<>();
    packageConfig.put("PACKAGE_SPEC", new PackageMaterialProperty().withValue("sample-package-1.0"));
    PackageRevisionMessage last = new PackageRevisionMessage("abc-10.2.1.rpm", Date.from(Instant.ofEpochSecond(0)), null, null, null);
    last.addData("VERSION", "5.3.0");
    last.addData("LOCATION", "http://www.sample.org/location/of/package");

    LatestPackageRevisionSinceMessage expected = new LatestPackageRevisionSinceMessage(repositoryConfig, packageConfig, last);

    assertTrue(isConfigEqual(actual.getPackageConfiguration().getPropertyMap(), expected.getPackageConfiguration().getPropertyMap()));
    assertTrue(isConfigEqual(actual.getRepositoryConfiguration().getPropertyMap(), expected.getRepositoryConfiguration().getPropertyMap()));
    assertThat(actual.getPreviousRevision(), is(expected.getPreviousRevision()));
  }

  private boolean isConfigEqual(Map<String, PackageMaterialProperty> config, Map<String, PackageMaterialProperty> other) {
    if (!config.keySet().equals(other.keySet())) {
      return false;
    }
    for (String key : config.keySet()) {
      if (!other.get(key).value().equals(config.get(key).value())) {
        return false;
      }
    }
    return true;
  }
}

