package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.Envelope;
import me.neznamy.tab.libs.com.rabbitmq.client.GetResponse;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

class MicrometerObservationCollector implements ObservationCollector {
   private final ObservationRegistry registry;
   private final PublishObservationConvention customPublishConvention;
   private final PublishObservationConvention defaultPublishConvention;
   private final DeliverObservationConvention customProcessConvention;
   private final DeliverObservationConvention defaultProcessConvention;
   private final DeliverObservationConvention customReceiveConvention;
   private final DeliverObservationConvention defaultReceiveConvention;
   private final boolean keepObservationOpenOnBasicGet;

   MicrometerObservationCollector(
      ObservationRegistry registry,
      PublishObservationConvention customPublishConvention,
      PublishObservationConvention defaultPublishConvention,
      DeliverObservationConvention customProcessConvention,
      DeliverObservationConvention defaultProcessConvention,
      DeliverObservationConvention customReceiveConvention,
      DeliverObservationConvention defaultReceiveConvention,
      boolean keepObservationOpenOnBasicGet
   ) {
      this.registry = registry;
      this.customPublishConvention = customPublishConvention;
      this.defaultPublishConvention = defaultPublishConvention;
      this.customProcessConvention = customProcessConvention;
      this.defaultProcessConvention = defaultProcessConvention;
      this.customReceiveConvention = customReceiveConvention;
      this.defaultReceiveConvention = defaultReceiveConvention;
      this.keepObservationOpenOnBasicGet = keepObservationOpenOnBasicGet;
   }

   @Override
   public void publish(
      ObservationCollector.PublishCall call,
      AMQP.Basic.Publish publish,
      AMQP.BasicProperties properties,
      byte[] body,
      ObservationCollector.ConnectionInfo connectionInfo
   ) throws IOException {
      Map<String, Object> headers;
      if (properties.getHeaders() == null) {
         headers = new HashMap<>();
      } else {
         headers = new HashMap<>(properties.getHeaders());
      }

      PublishContext micrometerPublishContext = new PublishContext(
         publish.getExchange(), publish.getRoutingKey(), headers, body == null ? 0 : body.length, connectionInfo
      );
      AMQP.BasicProperties.Builder builder = properties.builder();
      builder.headers(headers);
      Observation observation = RabbitMqObservationDocumentation.PUBLISH_OBSERVATION
         .observation(this.customPublishConvention, this.defaultPublishConvention, () -> micrometerPublishContext, this.registry);
      observation.start();

      try {
         call.publish(builder.build());
      } catch (IOException | AlreadyClosedException e) {
         observation.error(e);
         throw e;
      } finally {
         observation.stop();
      }
   }

   @Override
   public Consumer basicConsume(String queue, String consumerTag, Consumer consumer) {
      return new MicrometerObservationCollector.ObservationConsumer(queue, consumer, this.registry, this.customProcessConvention, this.defaultProcessConvention);
   }

   @Override
   public GetResponse basicGet(ObservationCollector.BasicGetCall call, String queue) {
      Observation observation = Observation.createNotStarted("rabbitmq.receive", this.registry)
         .highCardinalityKeyValues(
            KeyValues.of(
               new KeyValue[]{
                  RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_OPERATION.withValue("receive"),
                  RabbitMqObservationDocumentation.LowCardinalityTags.MESSAGING_SYSTEM.withValue("rabbitmq")
               }
            )
         )
         .start();
      boolean stopped = false;

      try {
         GetResponse response = call.get();
         if (response != null) {
            observation.stop();
            stopped = true;
            Map<String, Object> headers;
            if (response.getProps() != null && response.getProps().getHeaders() != null) {
               headers = response.getProps().getHeaders();
            } else {
               headers = Collections.emptyMap();
            }

            DeliverContext context = new DeliverContext(
               response.getEnvelope().getExchange(),
               response.getEnvelope().getRoutingKey(),
               queue,
               headers,
               response.getBody() == null ? 0 : response.getBody().length
            );
            Observation receiveObservation = RabbitMqObservationDocumentation.RECEIVE_OBSERVATION
               .observation(this.customReceiveConvention, this.defaultReceiveConvention, () -> context, this.registry);
            receiveObservation.start();
            if (this.keepObservationOpenOnBasicGet) {
               receiveObservation.openScope();
            } else {
               receiveObservation.stop();
            }
         }

         return response;
      } catch (RuntimeException e) {
         observation.error(e);
         throw e;
      } finally {
         if (!stopped) {
            observation.stop();
         }
      }
   }

   private static class ObservationConsumer implements Consumer {
      private final String queue;
      private final Consumer delegate;
      private final ObservationRegistry observationRegistry;
      private final DeliverObservationConvention customConsumeConvention;
      private final DeliverObservationConvention defaultConsumeConvention;

      private ObservationConsumer(
         String queue,
         Consumer delegate,
         ObservationRegistry observationRegistry,
         DeliverObservationConvention customConsumeConvention,
         DeliverObservationConvention defaultConsumeConvention
      ) {
         this.queue = queue;
         this.delegate = delegate;
         this.observationRegistry = observationRegistry;
         this.customConsumeConvention = customConsumeConvention;
         this.defaultConsumeConvention = defaultConsumeConvention;
      }

      @Override
      public void handleConsumeOk(String consumerTag) {
         this.delegate.handleConsumeOk(consumerTag);
      }

      @Override
      public void handleCancelOk(String consumerTag) {
         this.delegate.handleCancelOk(consumerTag);
      }

      @Override
      public void handleCancel(String consumerTag) throws IOException {
         this.delegate.handleCancel(consumerTag);
      }

      @Override
      public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
         this.delegate.handleShutdownSignal(consumerTag, sig);
      }

      @Override
      public void handleRecoverOk(String consumerTag) {
         this.delegate.handleRecoverOk(consumerTag);
      }

      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
         Map<String, Object> headers;
         if (properties != null && properties.getHeaders() != null) {
            headers = properties.getHeaders();
         } else {
            headers = Collections.emptyMap();
         }

         DeliverContext context = new DeliverContext(envelope.getExchange(), envelope.getRoutingKey(), this.queue, headers, body == null ? 0 : body.length);
         Observation observation = RabbitMqObservationDocumentation.PROCESS_OBSERVATION
            .observation(this.customConsumeConvention, this.defaultConsumeConvention, () -> context, this.observationRegistry);
         observation.observeChecked(() -> this.delegate.handleDelivery(consumerTag, envelope, properties, body));
      }
   }
}
