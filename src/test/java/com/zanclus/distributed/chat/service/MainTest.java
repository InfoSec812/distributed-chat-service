/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zanclus.distributed.chat.service;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 *
 * @author dphillips
 */
@RunWith(VertxUnitRunner.class)
public class MainTest {
    
    Vertx vertx;
    HttpClient client;

    @Before
    public void setup(TestContext context) {
        Async async = context.async();
        vertx = Vertx.vertx();
        vertx.deployVerticle(new Main(), complete -> {
            async.complete();
        });
    }

    /**
     * Test of start method, of class Main.
     */
    @Test
    public void testIndexPage(TestContext ctx) throws Exception {
        Async async = ctx.async();
        client = vertx.createHttpClient();
        client.getNow(8000, "localhost", "/", resp -> {
            resp.bodyHandler(body -> {
                String html = body.toString("UTF-8");
                ctx.assertTrue(html.contains("Distributed Chat Service"), "Body MUST be our 'chat.html'");
                async.complete();
            });
        });
    }

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
