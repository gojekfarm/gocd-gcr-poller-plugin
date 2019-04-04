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
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import com.thoughtworks.go.plugin.api.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class GoogleCredentialService {
  Logger logger = Logger.getLoggerFor(GoogleCredentialService.class);

  /**
   * Create GoogleCredentials instance which able to access GCR service.
   *
   * @param jsonServiceAccount JSON formatted service account string
   * @return GoogleCredentials instance.
   */
  public GoogleCredentials getGcrCredential(String jsonServiceAccount) {
    if (Strings.isNullOrEmpty(jsonServiceAccount)) {
      throw new IllegalArgumentException("jsonServiceAccount is null or empty");
    }

    GoogleCredentials googleCredentials;
    try {
      googleCredentials =
          GoogleCredentials.fromStream(new ByteArrayInputStream(jsonServiceAccount.getBytes()))
              .createScoped(
                  Arrays.asList(
                      "https://www.googleapis.com/auth/cloud-platform",
                      "https://www.googleapis.com/auth/devstorage.read_write"));

      googleCredentials.refreshIfExpired();
    } catch (IOException e) {
      logger.error("Unable to get GCR credential", e);
      throw new InvalidCredentialException("Unable to get GCR credential");
    }

    return googleCredentials;
  }
}
