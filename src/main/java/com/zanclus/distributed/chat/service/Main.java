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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.sockjs.BridgeOptions;
import io.vertx.ext.apex.handler.sockjs.PermittedOptions;
import io.vertx.ext.apex.handler.sockjs.SockJSHandler;

/**
 * A simple implementation of the Vert.x SockJS Event Bus Bridge for a chat site.
 * @author <a href="https://github.com/InfoSec812/">Deven Phillips</a>
 */
public class Main extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Main());
    }

    @Override
    public void start() throws Exception {
        final Logger log = LoggerFactory.getLogger(this.getClass());
        
        log.debug("Creating router");
        Router router = Router.router(vertx);
        
        final EventBus eb = vertx.eventBus();
        
        log.debug("Subscribing to 'chat.message.in'");
        eb.consumer("chat.message.in").handler(message -> {
            log.debug("Message recieved: "+message.body());
            eb.publish("chat.message.out", message.body());
        });
        
        log.debug("Creating route for 'chat.html'");
        router.route(HttpMethod.GET, "/chat.html").handler(req -> {
            req.response().sendFile("webroot/chat.html");
        });
        
        log.debug("Creating route for 'vertxbus.js'");
        router.route(HttpMethod.GET, "/vertxbus.js").handler(req -> {
            req.response().sendFile("webroot/vertxbus.js");
        });
        
        log.debug("Creating JS/EventBus bridge options");
        BridgeOptions opts = new BridgeOptions()
                                    .addInboundPermitted(new PermittedOptions().setAddress("chat.message.in"))
                                    .addOutboundPermitted(new PermittedOptions().setAddress("chat.message.out"));
        
        log.debug("Adding eventbus route");
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);
        
        log.debug("Starting http server.");
        vertx.createHttpServer().requestHandler(router::accept).listen(8000);
    }
}