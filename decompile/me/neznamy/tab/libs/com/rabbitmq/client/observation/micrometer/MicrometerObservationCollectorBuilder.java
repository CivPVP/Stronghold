package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.observation.ObservationRegistry;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;

public class MicrometerObservationCollectorBuilder {
   private ObservationRegistry registry = ObservationRegistry.NOOP;
   private PublishObservationConvention customPublishObservationConvention;
   private PublishObservationConvention defaultPublishObservationConvention = new DefaultPublishObservationConvention();
   private DeliverObservationConvention customProcessObservationConvention;
   private DeliverObservationConvention defaultProcessObservationConvention = new DefaultProcessObservationConvention("process");
   private DeliverObservationConvention customReceiveObservationConvention;
   private DeliverObservationConvention defaultReceiveObservationConvention = new DefaultReceiveObservationConvention("receive");
   private boolean keepObservationStartedOnBasicGet = false;

   public MicrometerObservationCollectorBuilder registry(ObservationRegistry registry) {
      this.registry = registry;
      return this;
   }

   public MicrometerObservationCollectorBuilder customPublishObservationConvention(PublishObservationConvention customPublishObservationConvention) {
      this.customPublishObservationConvention = customPublishObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder defaultPublishObservationConvention(PublishObservationConvention defaultPublishObservationConvention) {
      this.defaultPublishObservationConvention = defaultPublishObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder customProcessObservationConvention(DeliverObservationConvention customProcessObservationConvention) {
      this.customProcessObservationConvention = customProcessObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder defaultProcessObservationConvention(DeliverObservationConvention defaultProcessObservationConvention) {
      this.defaultProcessObservationConvention = defaultProcessObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder customReceiveObservationConvention(DeliverObservationConvention customReceiveObservationConvention) {
      this.customReceiveObservationConvention = customReceiveObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder defaultReceiveObservationConvention(DeliverObservationConvention defaultReceiveObservationConvention) {
      this.defaultReceiveObservationConvention = defaultReceiveObservationConvention;
      return this;
   }

   public MicrometerObservationCollectorBuilder keepObservationStartedOnBasicGet(boolean keepObservationStartedOnBasicGet) {
      this.keepObservationStartedOnBasicGet = keepObservationStartedOnBasicGet;
      return this;
   }

   public ObservationCollector build() {
      return new MicrometerObservationCollector(
         this.registry,
         this.customPublishObservationConvention,
         this.defaultPublishObservationConvention,
         this.customProcessObservationConvention,
         this.defaultProcessObservationConvention,
         this.customReceiveObservationConvention,
         this.defaultReceiveObservationConvention,
         this.keepObservationStartedOnBasicGet
      );
   }
}
