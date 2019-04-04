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

package com.gojek.gcrpoller.message;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessage {
    private final List<ValidationError> validationErrors = new ArrayList<>();

    public void addError(ValidationError validationError) {
        validationErrors.add(validationError);
    }

    public boolean failure() {
        return !validationErrors.isEmpty();
    }

    public List<String> getMessages() {
        List<String> errorMessages = new ArrayList<>();
        for (ValidationError error : validationErrors) {
            errorMessages.add(error.getMessage());
        }
        return errorMessages;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public Boolean success() {
        return !failure();
    }
}
