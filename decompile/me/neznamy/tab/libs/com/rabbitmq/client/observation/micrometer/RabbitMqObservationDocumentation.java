package me.neznamy.tab.libs.com.rabbitmq.client.observation.micrometer;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RabbitMqObservationDocumentation implements ObservationDocumentation {
   PUBLISH_OBSERVATION {
      public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
         return DefaultPublishObservationConvention.class;
      }

      public KeyName[] getLowCardinalityKeyNames() {
         return RabbitMqObservationDocumentation.LowCardinalityTags.values();
      }
   },
   PROCESS_OBSERVATION {
      public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
         return DefaultProcessObservationConvention.class;
      }

      public KeyName[] getLowCardinalityKeyNames() {
         return RabbitMqObservationDocumentation.LowCardinalityTags.values();
      }
   },
   RECEIVE_OBSERVATION {
      public Class<? extends ObservationConvention<? extends Context>> getDefaultConvention() {
         return DefaultReceiveObservationConvention.class;
      }

      public KeyName[] getLowCardinalityKeyNames() {
         return RabbitMqObservationDocumentation.LowCardinalityTags.values();
      }
   };

   RabbitMqObservationDocumentation() {
   }

   public enum HighCardinalityTags implements KeyName {
      MESSAGING_DESTINATION_NAME {
         public String asString() {
            return "messaging.destination.name";
         }
      },
      MESSAGING_ROUTING_KEY {
         public String asString() {
            return "messaging.rabbitmq.destination.routing_key";
         }
      },
      MESSAGING_SOURCE_NAME {
         public String asString() {
            return "messaging.source.name";
         }
      },
      MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES {
         public String asString() {
            return "messaging.message.payload_size_bytes";
         }
      },
      NET_SOCK_PEER_PORT {
         public String asString() {
            return "net.sock.peer.port";
         }
      },
      NET_SOCK_PEER_ADDR {
         public String asString() {
            return "net.sock.peer.addr";
         }
      };

      HighCardinalityTags() {
      }
   }

   public enum LowCardinalityTags implements KeyName {
      MESSAGING_SYSTEM {
         public String asString() {
            return "messaging.system";
         }
      },
      MESSAGING_OPERATION {
         public String asString() {
            return "messaging.operation";
         }
      },
      NET_PROTOCOL_NAME {
         public String asString() {
            return "net.protocol.name";
         }
      },
      NET_PROTOCOL_VERSION {
         public String asString() {
            return "net.protocol.version";
         }
      };

      LowCardinalityTags() {
      }
   }
}
