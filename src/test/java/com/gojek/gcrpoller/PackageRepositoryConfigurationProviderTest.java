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



import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.gojek.gcrpoller.message.PackageMaterialProperties;
import com.gojek.gcrpoller.message.PackageMaterialProperty;
import com.gojek.gcrpoller.message.ValidationError;
import com.gojek.gcrpoller.message.ValidationResultMessage;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class PackageRepositoryConfigurationProviderTest {

  @Test
  public void shouldReturnPackageConfigurationWithCorrectKeys() {
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();
    PackageMaterialProperties packageConfiguration = configProvider.packageConfiguration();
    Set<String> keys = packageConfiguration.getPropertyMap().keySet();
    HashSet<String> expectedKeys = Sets
        .newHashSet(PluginConfigurations.DOCKER_IMAGE, PluginConfigurations.DOCKER_TAG_FILTER);
    assertEquals(keys, expectedKeys);
  }

  @Test
  public void shouldReturnCorrectRepositoryConfiguration() {
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();
    PackageMaterialProperties repositoryConfiguration = configProvider.repositoryConfiguration();
    Set<String> keys = repositoryConfiguration.getPropertyMap().keySet();
    HashSet<String> expectedKeys = Sets
        .newHashSet(PluginConfigurations.GCP_SERVICE_ACCOUNT, PluginConfigurations.GCP_PROJECT,
            PluginConfigurations.GCP_REGISTRY_URL);
    assertEquals(keys, expectedKeys);
  }

  @Test
  public void shouldReturnValidationMessageWithErrorForMissingGcpServiceAccount() {
    PackageMaterialProperties config = new PackageMaterialProperties();
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();

    config.addPackageMaterialProperty(PluginConfigurations.GCP_REGISTRY_URL, new PackageMaterialProperty().withValue("gcr.io"));
    config.addPackageMaterialProperty(PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue("my-project"));
    ValidationResultMessage expected = new ValidationResultMessage();
    expected.addError(ValidationError.create(PluginConfigurations.GCP_SERVICE_ACCOUNT, "GCP service account key not provided"));
    ValidationResultMessage actual = configProvider.validateRepositoryConfiguration(config);

    assertTrue(actual.failure());
    assertThat(expected.getValidationErrors(), is(actual.getValidationErrors()));
  }

  @Test
  public void shouldReturnValidationMessageWithErrorForMissingGcpRegistryUrl() {
    PackageMaterialProperties config = new PackageMaterialProperties();
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();

    config.addPackageMaterialProperty(PluginConfigurations.GCP_PROJECT, new PackageMaterialProperty().withValue("my-project"));
    ValidationResultMessage expected = new ValidationResultMessage();

    expected.addError(ValidationError.create(PluginConfigurations.GCP_SERVICE_ACCOUNT, "GCP service account key not provided"));
    expected.addError(ValidationError.create(PluginConfigurations.GCP_REGISTRY_URL, "GCP registry url not specified"));
    ValidationResultMessage actual = configProvider.validateRepositoryConfiguration(config);

    assertTrue(actual.failure());
    assertThat(expected.getValidationErrors(), is(actual.getValidationErrors()));
  }

  @Test
  public void shouldReturnValidationMessageWithErrorForMissingGcpProject() {
    PackageMaterialProperties config = new PackageMaterialProperties();
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();

    config.addPackageMaterialProperty(PluginConfigurations.GCP_SERVICE_ACCOUNT, new PackageMaterialProperty().withValue("{}"));
    ValidationResultMessage expected = new ValidationResultMessage();

    expected.addError(ValidationError.create(PluginConfigurations.GCP_PROJECT, "GCP project not specified"));
    expected.addError(ValidationError.create(PluginConfigurations.GCP_REGISTRY_URL, "GCP registry url not specified"));
    ValidationResultMessage actual = configProvider.validateRepositoryConfiguration(config);

    assertTrue(actual.failure());
    assertThat(expected.getValidationErrors(), is(actual.getValidationErrors()));
  }

  @Test
  public void shouldReturnValidationMessageWithErrorForMissingDockerImage() {
    PackageMaterialProperties config = new PackageMaterialProperties();
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();

    ValidationResultMessage expected = new ValidationResultMessage();

    expected.addError(ValidationError.create(PluginConfigurations.DOCKER_IMAGE, "Docker image not specified"));
    ValidationResultMessage actual = configProvider.validatePackageConfiguration(config);

    assertTrue(actual.failure());
    assertThat(expected.getValidationErrors(), is(actual.getValidationErrors()));
  }

  @Test
  public void shouldReturnValidationMessageWithErrorForEmptyDockerImage() {
    PackageMaterialProperties config = new PackageMaterialProperties();
    PackageRepositoryConfigurationProvider configProvider = new PackageRepositoryConfigurationProvider();
    config.addPackageMaterialProperty(PluginConfigurations.DOCKER_IMAGE, new PackageMaterialProperty().withValue(""));

    ValidationResultMessage expected = new ValidationResultMessage();

    expected.addError(ValidationError.create(PluginConfigurations.DOCKER_IMAGE, "Docker image is empty"));
    ValidationResultMessage actual = configProvider.validatePackageConfiguration(config);

    assertTrue(actual.failure());
    assertThat(expected.getValidationErrors(), is(actual.getValidationErrors()));
  }
}