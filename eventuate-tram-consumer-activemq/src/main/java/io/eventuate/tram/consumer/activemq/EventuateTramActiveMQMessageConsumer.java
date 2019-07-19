package io.eventuate.tram.consumer.activemq;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public class EventuateTramActiveMQMessageConsumer implements MessageConsumerImplementation {

  private MessageConsumerActiveMQImpl messageConsumerActiveMQ;

  public EventuateTramActiveMQMessageConsumer(MessageConsumerActiveMQImpl messageConsumerActiveMQ) {
    this.messageConsumerActiveMQ = messageConsumerActiveMQ;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Subscription subscription = messageConsumerActiveMQ.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerActiveMQ.getId();
  }

  @Override
  public void close() {
    messageConsumerActiveMQ.close();
  }
}
