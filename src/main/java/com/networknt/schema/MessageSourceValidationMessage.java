/*
 * Copyright (c) 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.networknt.schema.i18n.MessageSource;

public class MessageSourceValidationMessage {

    public static Builder builder(MessageSource messageSource, Map<String, String> errorMessage,
            Consumer<ValidationMessage> observer) {
        return new Builder(messageSource, errorMessage, observer);
    }

    public static class Builder extends BuilderSupport<Builder> {
        public Builder(MessageSource messageSource, Map<String, String> errorMessage,
                Consumer<ValidationMessage> observer) {
            super(messageSource, errorMessage, observer);
        }

        @Override
        public Builder self() {
            return this;
        }
    }

    public abstract static class BuilderSupport<S> extends ValidationMessage.BuilderSupport<S> {
        private final Consumer<ValidationMessage> observer;
        private final MessageSource messageSource;
        private final Map<String, String> errorMessage;
        private Locale locale;

        public BuilderSupport(MessageSource messageSource, Map<String, String> errorMessage,
                Consumer<ValidationMessage> observer) {
            this.messageSource = messageSource;
            this.observer = observer;
            this.errorMessage = errorMessage;
        }

        @Override
        public ValidationMessage build() {
            // Use custom error message if present
            String messagePattern = null;
            if (this.errorMessage != null) {
                messagePattern = this.errorMessage.get("");
                if (this.property != null) {
                    String specificMessagePattern = this.errorMessage.get(this.property);
                    if (specificMessagePattern != null) {
                        messagePattern = specificMessagePattern;
                    }
                }
                this.message = messagePattern;
            }

            // Default to message source formatter
            if (this.message == null && this.messageSupplier == null && this.messageFormatter == null) {
                this.messageFormatter = args -> this.messageSource.getMessage(this.messageKey,
                        this.locale == null ? Locale.ROOT : this.locale, args);
            }
            ValidationMessage validationMessage = super.build();
            if (this.observer != null) {
                this.observer.accept(validationMessage);
            }
            return validationMessage;
        }

        public S locale(Locale locale) {
            this.locale = locale;
            return self();
        }
    }
}
