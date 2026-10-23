/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.internal;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CountTokensTest {
    @Test
    void equalOwnersAndReplacedNumbersKeepIndependentProvenance() {
        ObjectNode a = JsonNodeFactory.instance.objectNode().put("minItems", 1.0);
        ObjectNode b = JsonNodeFactory.instance.objectNode().put("minItems", 1.0);
        assertEquals(a, b);
        CountTokens.put(a, "minItems", a.get("minItems"), "0.99999999999999999999");
        CountTokens.put(b, "minItems", b.get("minItems"), "1.0");
        assertEquals("0.99999999999999999999", CountTokens.get(a, "minItems", a.get("minItems")));
        assertEquals("1.0", CountTokens.get(b, "minItems", b.get("minItems")));
        a.put("minItems", 2);
        assertNull(CountTokens.get(a, "minItems", a.get("minItems")));
        assertEquals("1.0", CountTokens.get(b, "minItems", b.get("minItems")));
    }

    @Test
    void changingAMutableNumericNodeInvalidatesItsSnapshot() {
        ObjectNode owner = JsonNodeFactory.instance.objectNode();
        MutableNumber node = new MutableNumber();
        owner.set("minItems", node);
        CountTokens.put(owner, "minItems", node, "3000000000.0");
        node.value = 2;
        assertNull(CountTokens.get(owner, "minItems", node));
    }

    private static final class MutableNumber extends DoubleNode {
        private static final long serialVersionUID = 1L;
        double value = 3000000000.0;
        MutableNumber() { super(3000000000.0); }
        @Override public double doubleValue() { return value; }
        @Override public Number numberValue() { return Double.valueOf(value); }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> entries() throws Exception {
        Field field = CountTokens.class.getDeclaredField("TOKENS");
        field.setAccessible(true);
        return (Map<Object, Object>) field.get(null);
    }

    private static final class Released {
        final WeakReference<JsonNode> owner;
        final Object key;
        Released(WeakReference<JsonNode> owner, Object key) { this.owner = owner; this.key = key; }
    }

    private static Released registerCycleAndReleaseIt() throws Exception {
        ObjectNode owner = JsonNodeFactory.instance.objectNode();
        DoubleNode number = new OwnerHoldingNumber(owner);
        owner.set("minItems", number);
        CountTokens.put(owner, "minItems", number, "1.0");
        synchronized (CountTokens.class) {
            for (Object key : entries().keySet()) {
                if (((WeakReference<?>) key).get() == owner) {
                    return new Released(new WeakReference<>(owner), key);
                }
            }
        }
        throw new AssertionError("registered owner not found");
    }

    private static final class OwnerHoldingNumber extends DoubleNode {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused") private final JsonNode owner;
        OwnerHoldingNumber(JsonNode owner) { super(1.0); this.owner = owner; }
    }

    @Test
    void releasedOwnersAreNotRetainedEvenThroughCustomNumericNodes() throws Exception {
        Released released = registerCycleAndReleaseIt();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (released.owner.get() != null && System.nanoTime() < deadline) {
            System.gc();
            Thread.sleep(10);
        }
        assertNull(released.owner.get(), "sidecar retained a schema owner");
        ObjectNode touch = JsonNodeFactory.instance.objectNode().put("minItems", 1);
        deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        boolean removed = false;
        while (!removed && System.nanoTime() < deadline) {
            CountTokens.get(touch, "minItems", touch.get("minItems"));
            synchronized (CountTokens.class) {
                removed = !entries().containsKey(released.key);
            }
            if (!removed) {
                Thread.sleep(10); // Reference clearing and queue enqueueing need not be simultaneous.
            }
        }
        assertTrue(removed, "queued owner entry was not drained");
    }

    @Test
    void parallelCompilesWithGcDoNotCrossTalk() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        ScheduledExecutorService gc = Executors.newSingleThreadScheduledExecutor();
        try {
            gc.scheduleWithFixedDelay(System::gc, 0, 100, TimeUnit.MILLISECONDS);
            List<Future<?>> tasks = new ArrayList<>();
            for (int thread = 0; thread < 8; thread++) {
                final int worker = thread;
                tasks.add(pool.submit(() -> {
                    SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
                    for (int i = 0; i < 100; i++) {
                        Schema fraction = registry.getSchema("{\"maxItems\":0.99999999999999999999}");
                        assertTrue(fraction.validate("[1,2]", InputFormat.JSON).isEmpty());
                        String bound = Long.toString(3000000000L + 100 * worker + i);
                        Schema minimum = registry.getSchema("{\"minItems\":" + bound + ".0}");
                        assertEquals(bound, minimum.validate("[]", InputFormat.JSON).get(0).getArguments()[0]);
                    }
                }));
            }
            for (Future<?> task : tasks) { task.get(30, TimeUnit.SECONDS); }
        } finally {
            pool.shutdownNow();
            gc.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
            assertTrue(gc.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
