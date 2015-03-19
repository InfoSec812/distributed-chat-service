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
import static java.text.SimpleDateFormat.*;
import static java.time.Instant.now;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.handler.sockjs.BridgeOptions;
import io.vertx.ext.apex.handler.sockjs.PermittedOptions;
import io.vertx.ext.apex.handler.sockjs.SockJSHandler;
import java.util.Date;

/**
 * A simple implementation of the Vert.x SockJS Event Bus Bridge for a chat site.
 * @author <a href="https://github.com/InfoSec812/">Deven Phillips</a>
 */
public class Main extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Main());
//        Vertx.vertx().deployVerticle("chatverticle.groovy");
//        Vertx.vertx().deployVerticle("chat.js");
    }

    @Override
    public void start() throws Exception {
        
        Router router = Router.router(vertx);
        
        final EventBus eb = vertx.eventBus();
        
        eb.consumer("chat.to.server").handler(message -> {
            String timestamp = getDateTimeInstance(SHORT, MEDIUM).format(Date.from(now()));
            eb.publish("chat.to.client", timestamp+": "+message.body());
        });
        
        BridgeOptions opts = new BridgeOptions()
                                    .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                                    .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));
        
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);
        
        router.route().handler(StaticHandler.create("webroot/").setIndexPage("chat.html"));
        
        vertx.createHttpServer().requestHandler(router::accept).listen(8000);
    }
}