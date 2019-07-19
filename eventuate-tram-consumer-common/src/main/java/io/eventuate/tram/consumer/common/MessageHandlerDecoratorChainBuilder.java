package io.eventuate.tram.consumer.common;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MessageHandlerDecoratorChainBuilder {


  private List<MessageHandlerDecorator> handlers = new LinkedList<>();

  public static MessageHandlerDecoratorChainBuilder startingWith(MessageHandlerDecorator smh) {
    MessageHandlerDecoratorChainBuilder b = new MessageHandlerDecoratorChainBuilder();
    b.add(smh);
    return b;

  }

  private void add(MessageHandlerDecorator smh) {
    this.handlers.add(smh);
  }

  public MessageHandlerDecoratorChainBuilder andThen(MessageHandlerDecorator smh) {
    this.add(smh);
    return this;
  }

  public MessageHandlerDecoratorChain andFinally(Consumer<SubscriberIdAndMessage> consumer) {
    return buildChain(handlers, consumer);
  }

  private MessageHandlerDecoratorChain buildChain(List<MessageHandlerDecorator> handlers, Consumer<SubscriberIdAndMessage> consumer) {
    if  (handlers.isEmpty())
      return consumer::accept;
    else {
      MessageHandlerDecorator head = handlers.get(0);
      List<MessageHandlerDecorator> tail = handlers.subList(1, handlers.size());
      return subscriberIdAndMessage -> head.accept(subscriberIdAndMessage, buildChain(tail, consumer));
    }
  }
}
