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
import static java.text.DateFormat.*
import io.vertx.groovy.ext.apex.Router
import io.vertx.groovy.ext.apex.handler.sockjs.SockJSHandler
import io.vertx.groovy.ext.apex.handler.StaticHandler
import java.text.DateFormat

// Create an apex {@link Router}
def router = Router.router(vertx)

// Get the cached EventBus instance from Vertx.
def eb = vertx.eventBus()

// Register a listener on the {@link EventBus} to recieve messages from the client.
eb.consumer("chat.to.server").handler({ message ->
  // When a message is recieved, prepend a timestamp and send the message back to all clients.
  def now = java.util.Date.from(java.time.Instant.now())
  def timestamp = getDateTimeInstance(SHORT, MEDIUM).format(now)
  eb.publish("chat.to.client", timestamp+': '+message.body())
})

// Configure the {@link EventBus} bridge allowing only the specified addresses in/out.
def opts = [
  inboundPermitteds:[[address:"chat.to.server"]],
  outboundPermitteds:[[address:"chat.to.client"]]
]

def ebHandler = SockJSHandler.create(vertx).bridge(opts)
router.route("/eventbus/*").handler(ebHandler)

router.route().handler(StaticHandler.create().setIndexPage("chat.html"))

vertx.createHttpServer().requestHandler(router.&accept).listen(8000)
