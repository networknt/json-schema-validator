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

package com.networknt.schema.utils;

import java.util.function.Supplier;

/**
 * {@link Supplier} that caches the value.
 * <p>
 * This is not threadsafe.
 * 
 * @param <T> the type of results supplied by this supplier
 */
public class CachingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private T cached = null;

    public CachingSupplier(Supplier<T> supplier) {
        this.delegate = supplier;
    }

    @Override
    public T get() {
        if (this.cached == null) {
            this.cached = this.delegate.get();
        }
        return this.cached;
    }
}
