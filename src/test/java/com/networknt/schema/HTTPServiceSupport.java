/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import java.io.File;

import static io.undertow.Handlers.*;

abstract class HTTPServiceSupport {

    protected static Undertow server = null;

    @BeforeAll
    static void setUp() {
        if (server == null) {
            PathHandler pathHandler = path(resource(
                new FileResourceManager(
                    new File("./src/test/suite/remotes"),
                    100
                ))
            );

            pathHandler.addPrefixPath("folder", resource(
                new FileResourceManager(
                    new File("./src/test/resources/remotes/folder"),
                    100
                ))
            );

            pathHandler.addPrefixPath("id_schema", resource(
                new FileResourceManager(
                    new File("./src/test/resources/remotes/id_schema"),
                    100
                ))
            );

            pathHandler.addPrefixPath("self_ref", resource(
                new FileResourceManager(
                    new File("./src/test/resources/remotes/self_ref"),
                    100
                ))
            );

            server = Undertow.builder()
                .addHttpListener(1234, "localhost")
                .setHandler(pathHandler)
                .build();
            server.start();
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (server != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            server.stop();
            server = null;
        }
    }

    protected void cleanup() {
    }
}
