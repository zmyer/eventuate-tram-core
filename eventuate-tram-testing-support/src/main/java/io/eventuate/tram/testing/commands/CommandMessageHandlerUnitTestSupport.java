package io.eventuate.tram.testing.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.util.SimpleIdGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class CommandMessageHandlerUnitTestSupport {
  private MessageHandler handler;
  private CommandDispatcher dispatcher;
  private SimpleIdGenerator idGenerator = new SimpleIdGenerator();

  private String replyDestination;
  private Message replyMessage;
  private CommandProducer producer;

  public static CommandMessageHandlerUnitTestSupport given() {
    return new CommandMessageHandlerUnitTestSupport();
  }

  public CommandMessageHandlerUnitTestSupport commandHandlers(CommandHandlers commandHandlers) {
    this.dispatcher = new CommandDispatcher("mockCommandDispatcher-" + System.currentTimeMillis(),
            commandHandlers,
            new MessageConsumer() {
              @Override
              public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
                CommandMessageHandlerUnitTestSupport.this.handler = handler;
                return () -> {};
              }

              @Override
              public String getId() {
                return null;
              }

              @Override
              public void close() {

              }
            },
            (destination, message) -> {
              CommandMessageHandlerUnitTestSupport.this.replyDestination = destination;
              CommandMessageHandlerUnitTestSupport.this.replyMessage = message;
            });

    dispatcher.initialize();
    producer = new CommandProducerImpl((destination, message) -> {
      String id = idGenerator.generateId().toString();
      message.getHeaders().put(Message.ID, id);
      handler.accept(message);
    });

    return this;
  }

  public CommandMessageHandlerUnitTestSupport when() {
    return this;
  }

  public CommandMessageHandlerUnitTestSupport then() {
    return this;
  }


  public CommandMessageHandlerUnitTestSupport receives(Command command) {
    producer.send("don't care", command, "reply_to", Collections.emptyMap());
    return this;
  }

  public CommandMessageHandlerUnitTestSupport verify(Consumer<Message> c) {
    c.accept(replyMessage);
    return this;
  }

  public static void assertReplyTypeEquals(Class<?> replyType, Message reply) {
    assertEquals(replyType.getName(), reply.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE));
  }


}
