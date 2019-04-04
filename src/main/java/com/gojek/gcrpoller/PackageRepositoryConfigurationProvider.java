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

import com.gojek.gcrpoller.message.ValidationError;
import com.gojek.gcrpoller.message.ValidationResultMessage;
import com.gojek.gcrpoller.message.PackageMaterialProperties;
import com.gojek.gcrpoller.message.PackageMaterialProperty;

public class PackageRepositoryConfigurationProvider {

    public PackageMaterialProperties repositoryConfiguration() {
        PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
        repositoryConfigurationResponse.addPackageMaterialProperty(
                PluginConfigurations.GCP_SERVICE_ACCOUNT,
                new PackageMaterialProperty()
                        .withDisplayName("GCP Service Account Key")
                        .withDisplayOrder("0")
                        .withPartOfIdentity(false)
                        .withRequired(true)
                        .withSecure(true)
        );
        repositoryConfigurationResponse.addPackageMaterialProperty(
                PluginConfigurations.GCP_PROJECT,
                new PackageMaterialProperty()
                        .withDisplayName("GCP project id")
                        .withDisplayOrder("1")
                        .withPartOfIdentity(true)
                        .withRequired(true)
        );
        repositoryConfigurationResponse.addPackageMaterialProperty(
                PluginConfigurations.GCP_REGISTRY_URL,
                new PackageMaterialProperty()
                        .withDisplayName("GCR url")
                        .withDisplayOrder("2")
                        .withPartOfIdentity(true)
                        .withRequired(true)
        );
        return repositoryConfigurationResponse;
    }

    public PackageMaterialProperties packageConfiguration() {
        PackageMaterialProperties packageConfigurationResponse = new PackageMaterialProperties();
        packageConfigurationResponse.addPackageMaterialProperty(
                PluginConfigurations.DOCKER_IMAGE,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Image Name")
                        .withDisplayOrder("0")
                        .withPartOfIdentity(true)
                        .withRequired(true)
        );
        packageConfigurationResponse.addPackageMaterialProperty(
                PluginConfigurations.DOCKER_TAG_FILTER,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Tag Filter Regular Expression")
                        .withDisplayOrder("1")
                        .withPartOfIdentity(true)
                        .withRequired(false)
        );
        return packageConfigurationResponse;
    }

    public ValidationResultMessage validateRepositoryConfiguration(PackageMaterialProperties configurationProvidedByUser) {

        ValidationResultMessage validationResultMessage = new ValidationResultMessage();

        PackageMaterialProperty gcpServiceAccount = configurationProvidedByUser.getProperty(
            PluginConfigurations.GCP_SERVICE_ACCOUNT);
        PackageMaterialProperty gcpProject = configurationProvidedByUser.getProperty(
            PluginConfigurations.GCP_PROJECT);
        PackageMaterialProperty gcpRegistryUrl = configurationProvidedByUser.getProperty(
            PluginConfigurations.GCP_REGISTRY_URL);

        if (gcpServiceAccount == null) {
            validationResultMessage.addError(
                    ValidationError.create(PluginConfigurations.GCP_SERVICE_ACCOUNT, "GCP service account key not provided")
            );
        }
        if (gcpProject == null) {
            validationResultMessage.addError(
                    ValidationError.create(PluginConfigurations.GCP_PROJECT, "GCP project not specified")
            );
        }
        if (gcpRegistryUrl == null) {
            validationResultMessage.addError(
                ValidationError.create(PluginConfigurations.GCP_REGISTRY_URL, "GCP registry url not specified")
            );
        }
        return validationResultMessage;

    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        PackageMaterialProperty imageConfig = configurationProvidedByUser.getProperty(
            PluginConfigurations.DOCKER_IMAGE);
        if (imageConfig == null) {
            validationResultMessage.addError(ValidationError.create(PluginConfigurations.DOCKER_IMAGE, "Docker image not specified"));
            return validationResultMessage;
        }
        String image = imageConfig.value();
        if (image == null) {
            validationResultMessage.addError(ValidationError.create(PluginConfigurations.DOCKER_IMAGE, "Docker image is null"));
            return validationResultMessage;
        }
        if (image.trim().isEmpty()) {
            validationResultMessage.addError(ValidationError.create(PluginConfigurations.DOCKER_IMAGE, "Docker image is empty"));
            return validationResultMessage;
        }
        return validationResultMessage;
    }

}