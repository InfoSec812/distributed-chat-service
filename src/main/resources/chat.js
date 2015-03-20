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

var Router = require("vertx-apex-js/router");
var SockJSHandler = require("vertx-apex-js/sock_js_handler");
var StaticHandler = require("vertx-apex-js/static_handler");

// Create an apex {@link Router}
var router = Router.router(vertx);

// Get the cached EventBus instance from Vertx.
var eb = vertx.eventBus();

// Register a listener on the {@link EventBus} to recieve messages from the client.
eb.consumer("chat.to.server").handler(function (message) {
    // When a message is received, prepend a timestamp and send the message back to all clients.
    console.log(message.body())
    var now = Java.type("java.util.Date")
            .from(Java.type("java.time.Instant").now());
    var timestamp = Java.type("java.text.DateFormat")
            .getDateTimeInstance(Java.type("java.text.DateFormat").SHORT,
                    Java.type("java.text.DateFormat").MEDIUM).format(now);
    eb.publish("chat.to.client", timestamp + ": " + message.body());
});

// Configure the {@link EventBus} bridge allowing only the specified addresses in/out.
var opts = {
    "inboundPermitteds": [{"address": "chat.to.server"}],
    "outboundPermitteds": [{"address": "chat.to.client"}]
};

var ebHandler = SockJSHandler.create(vertx).bridge(opts);
router.route("/eventbus/*").handler(ebHandler.handle);

router.route()
        .handler(StaticHandler.create("webroot/")
                .setIndexPage("chat.html").handle);

vertx.createHttpServer().requestHandler(router.accept).listen(8000);