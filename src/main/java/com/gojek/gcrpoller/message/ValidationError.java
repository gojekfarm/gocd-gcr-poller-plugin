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

import com.google.gson.annotations.Expose;

public class ValidationError {

    @Expose
    private final String key;

    @Expose
    private final String message;

    private ValidationError(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public static ValidationError create(String message) {
        return new ValidationError("", message);
    }

    public static ValidationError create(String key, String message) {
        return new ValidationError(key, message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationError that = (ValidationError) o;

        return (key != null ? key.equals(that.key) : that.key == null) &&
                (message != null ? message.equals(that.message) : that.message == null);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}