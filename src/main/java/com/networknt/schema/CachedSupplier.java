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

import java.util.function.Supplier;

/**
 * Supplier that caches the output.
 * 
 * @param <T> the type cached
 */
public class CachedSupplier<T> implements Supplier<T> {
    private volatile Supplier<T> delegate;
    private volatile T cache = null;

    public CachedSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (this.delegate == null) {
            return this.cache;
        }

        synchronized(this) {
            if (this.delegate != null) {
                this.cache = this.delegate.get();
                this.delegate = null;
            }
        }
        return this.cache;
    }
}
