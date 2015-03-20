/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zanclus.distributed.chat.service;

/*
 * #%L
 * distributed-chat-service
 * %%
 * Copyright (C) 2015 Zanclus Consulting
 * %%
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
 * #L%
 */

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author dphillips
 */
@RunWith(VertxUnitRunner.class)
public class MainTest {
    
    Vertx vertx;

    @Before
    public void setup(TestContext context) {
        Async async = context.async();
        vertx = Vertx.vertx();
        vertx.deployVerticle(new Main(), complete -> {
            async.complete();
        });
    }

    /**
     * Test the return of the static HTML content.
     * @param ctx The Vert.x testing context.
     * @throws Exception
     */
    @Test
    public void testIndexPage(TestContext ctx) throws Exception {
        Async async = ctx.async();
        HttpClient client = vertx.createHttpClient();
        client.getNow(8000, "localhost", "/", resp -> {
            resp.bodyHandler(body -> {
                String html = body.toString("UTF-8");
                ctx.assertTrue(html.contains("Distributed Chat Service"), "Body MUST be our 'chat.html'");
                async.complete();
            });
        });
    }

    /**
     * Send a message to the 'chat.to.server' address and expect back the same message prepended with a timestamp.
     * @param ctx The Vert.x testing context.
     * @throws Exception 
     */
    @Test
    public void testEventBus(TestContext ctx) throws Exception {
        Async async = ctx.async();
        vertx.eventBus().consumer("chat.to.client", (Message<String> handler) -> {
            String clientMsg = handler.body();
            ctx.assertTrue(clientMsg.endsWith("TEST MESSAGE"), "Test message MUST match expected string.");
            async.complete();
        });
        vertx.eventBus().send("chat.to.server", "TEST MESSAGE");
    }
}
