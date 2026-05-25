package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQBasicProperties;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQImpl;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ContentHeaderPropertyReader;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ContentHeaderPropertyWriter;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.LongStringHelper;

public interface AMQP {
   int FRAME_METHOD = 1;
   int FRAME_HEADER = 2;
   int FRAME_BODY = 3;
   int FRAME_HEARTBEAT = 8;
   int FRAME_MIN_SIZE = 4096;
   int FRAME_END = 206;
   int REPLY_SUCCESS = 200;
   int CONTENT_TOO_LARGE = 311;
   int NO_ROUTE = 312;
   int NO_CONSUMERS = 313;
   int ACCESS_REFUSED = 403;
   int NOT_FOUND = 404;
   int RESOURCE_LOCKED = 405;
   int PRECONDITION_FAILED = 406;
   int CONNECTION_FORCED = 320;
   int INVALID_PATH = 402;
   int FRAME_ERROR = 501;
   int SYNTAX_ERROR = 502;
   int COMMAND_INVALID = 503;
   int CHANNEL_ERROR = 504;
   int UNEXPECTED_FRAME = 505;
   int RESOURCE_ERROR = 506;
   int NOT_ALLOWED = 530;
   int NOT_IMPLEMENTED = 540;
   int INTERNAL_ERROR = 541;

   class Access {
      public interface Request extends Method {
         String getRealm();

         boolean getExclusive();

         boolean getPassive();

         boolean getActive();

         boolean getWrite();

         boolean getRead();

         final class Builder {
            private String realm = "/data";
            private boolean exclusive = false;
            private boolean passive = true;
            private boolean active = true;
            private boolean write = true;
            private boolean read = true;

            public AMQP.Access.Request.Builder realm(String realm) {
               this.realm = realm;
               return this;
            }

            public AMQP.Access.Request.Builder exclusive(boolean exclusive) {
               this.exclusive = exclusive;
               return this;
            }

            public AMQP.Access.Request.Builder exclusive() {
               return this.exclusive(true);
            }

            public AMQP.Access.Request.Builder passive(boolean passive) {
               this.passive = passive;
               return this;
            }

            public AMQP.Access.Request.Builder passive() {
               return this.passive(true);
            }

            public AMQP.Access.Request.Builder active(boolean active) {
               this.active = active;
               return this;
            }

            public AMQP.Access.Request.Builder active() {
               return this.active(true);
            }

            public AMQP.Access.Request.Builder write(boolean write) {
               this.write = write;
               return this;
            }

            public AMQP.Access.Request.Builder write() {
               return this.write(true);
            }

            public AMQP.Access.Request.Builder read(boolean read) {
               this.read = read;
               return this;
            }

            public AMQP.Access.Request.Builder read() {
               return this.read(true);
            }

            public AMQP.Access.Request build() {
               return new AMQImpl.Access.Request(this.realm, this.exclusive, this.passive, this.active, this.write, this.read);
            }
         }
      }

      public interface RequestOk extends Method {
         int getTicket();

         final class Builder {
            private int ticket = 1;

            public AMQP.Access.RequestOk.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Access.RequestOk build() {
               return new AMQImpl.Access.RequestOk(this.ticket);
            }
         }
      }
   }

   class Basic {
      public interface Ack extends Method {
         long getDeliveryTag();

         boolean getMultiple();

         final class Builder {
            private long deliveryTag = 0L;
            private boolean multiple = false;

            public AMQP.Basic.Ack.Builder deliveryTag(long deliveryTag) {
               this.deliveryTag = deliveryTag;
               return this;
            }

            public AMQP.Basic.Ack.Builder multiple(boolean multiple) {
               this.multiple = multiple;
               return this;
            }

            public AMQP.Basic.Ack.Builder multiple() {
               return this.multiple(true);
            }

            public AMQP.Basic.Ack build() {
               return new AMQImpl.Basic.Ack(this.deliveryTag, this.multiple);
            }
         }
      }

      public interface Cancel extends Method {
         String getConsumerTag();

         boolean getNowait();

         final class Builder {
            private String consumerTag;
            private boolean nowait = false;

            public AMQP.Basic.Cancel.Builder consumerTag(String consumerTag) {
               this.consumerTag = consumerTag;
               return this;
            }

            public AMQP.Basic.Cancel.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Basic.Cancel.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Basic.Cancel build() {
               return new AMQImpl.Basic.Cancel(this.consumerTag, this.nowait);
            }
         }
      }

      public interface CancelOk extends Method {
         String getConsumerTag();

         final class Builder {
            private String consumerTag;

            public AMQP.Basic.CancelOk.Builder consumerTag(String consumerTag) {
               this.consumerTag = consumerTag;
               return this;
            }

            public AMQP.Basic.CancelOk build() {
               return new AMQImpl.Basic.CancelOk(this.consumerTag);
            }
         }
      }

      public interface Consume extends Method {
         int getTicket();

         String getQueue();

         String getConsumerTag();

         boolean getNoLocal();

         boolean getNoAck();

         boolean getExclusive();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private String consumerTag = "";
            private boolean noLocal = false;
            private boolean noAck = false;
            private boolean exclusive = false;
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Basic.Consume.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Basic.Consume.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Basic.Consume.Builder consumerTag(String consumerTag) {
               this.consumerTag = consumerTag;
               return this;
            }

            public AMQP.Basic.Consume.Builder noLocal(boolean noLocal) {
               this.noLocal = noLocal;
               return this;
            }

            public AMQP.Basic.Consume.Builder noLocal() {
               return this.noLocal(true);
            }

            public AMQP.Basic.Consume.Builder noAck(boolean noAck) {
               this.noAck = noAck;
               return this;
            }

            public AMQP.Basic.Consume.Builder noAck() {
               return this.noAck(true);
            }

            public AMQP.Basic.Consume.Builder exclusive(boolean exclusive) {
               this.exclusive = exclusive;
               return this;
            }

            public AMQP.Basic.Consume.Builder exclusive() {
               return this.exclusive(true);
            }

            public AMQP.Basic.Consume.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Basic.Consume.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Basic.Consume.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Basic.Consume build() {
               return new AMQImpl.Basic.Consume(
                  this.ticket, this.queue, this.consumerTag, this.noLocal, this.noAck, this.exclusive, this.nowait, this.arguments
               );
            }
         }
      }

      public interface ConsumeOk extends Method {
         String getConsumerTag();

         final class Builder {
            private String consumerTag;

            public AMQP.Basic.ConsumeOk.Builder consumerTag(String consumerTag) {
               this.consumerTag = consumerTag;
               return this;
            }

            public AMQP.Basic.ConsumeOk build() {
               return new AMQImpl.Basic.ConsumeOk(this.consumerTag);
            }
         }
      }

      public interface Deliver extends Method {
         String getConsumerTag();

         long getDeliveryTag();

         boolean getRedelivered();

         String getExchange();

         String getRoutingKey();

         final class Builder {
            private String consumerTag;
            private long deliveryTag;
            private boolean redelivered = false;
            private String exchange;
            private String routingKey;

            public AMQP.Basic.Deliver.Builder consumerTag(String consumerTag) {
               this.consumerTag = consumerTag;
               return this;
            }

            public AMQP.Basic.Deliver.Builder deliveryTag(long deliveryTag) {
               this.deliveryTag = deliveryTag;
               return this;
            }

            public AMQP.Basic.Deliver.Builder redelivered(boolean redelivered) {
               this.redelivered = redelivered;
               return this;
            }

            public AMQP.Basic.Deliver.Builder redelivered() {
               return this.redelivered(true);
            }

            public AMQP.Basic.Deliver.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Basic.Deliver.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Basic.Deliver build() {
               return new AMQImpl.Basic.Deliver(this.consumerTag, this.deliveryTag, this.redelivered, this.exchange, this.routingKey);
            }
         }
      }

      public interface Get extends Method {
         int getTicket();

         String getQueue();

         boolean getNoAck();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private boolean noAck = false;

            public AMQP.Basic.Get.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Basic.Get.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Basic.Get.Builder noAck(boolean noAck) {
               this.noAck = noAck;
               return this;
            }

            public AMQP.Basic.Get.Builder noAck() {
               return this.noAck(true);
            }

            public AMQP.Basic.Get build() {
               return new AMQImpl.Basic.Get(this.ticket, this.queue, this.noAck);
            }
         }
      }

      public interface GetEmpty extends Method {
         String getClusterId();

         final class Builder {
            private String clusterId = "";

            public AMQP.Basic.GetEmpty.Builder clusterId(String clusterId) {
               this.clusterId = clusterId;
               return this;
            }

            public AMQP.Basic.GetEmpty build() {
               return new AMQImpl.Basic.GetEmpty(this.clusterId);
            }
         }
      }

      public interface GetOk extends Method {
         long getDeliveryTag();

         boolean getRedelivered();

         String getExchange();

         String getRoutingKey();

         int getMessageCount();

         final class Builder {
            private long deliveryTag;
            private boolean redelivered = false;
            private String exchange;
            private String routingKey;
            private int messageCount;

            public AMQP.Basic.GetOk.Builder deliveryTag(long deliveryTag) {
               this.deliveryTag = deliveryTag;
               return this;
            }

            public AMQP.Basic.GetOk.Builder redelivered(boolean redelivered) {
               this.redelivered = redelivered;
               return this;
            }

            public AMQP.Basic.GetOk.Builder redelivered() {
               return this.redelivered(true);
            }

            public AMQP.Basic.GetOk.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Basic.GetOk.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Basic.GetOk.Builder messageCount(int messageCount) {
               this.messageCount = messageCount;
               return this;
            }

            public AMQP.Basic.GetOk build() {
               return new AMQImpl.Basic.GetOk(this.deliveryTag, this.redelivered, this.exchange, this.routingKey, this.messageCount);
            }
         }
      }

      public interface Nack extends Method {
         long getDeliveryTag();

         boolean getMultiple();

         boolean getRequeue();

         final class Builder {
            private long deliveryTag = 0L;
            private boolean multiple = false;
            private boolean requeue = true;

            public AMQP.Basic.Nack.Builder deliveryTag(long deliveryTag) {
               this.deliveryTag = deliveryTag;
               return this;
            }

            public AMQP.Basic.Nack.Builder multiple(boolean multiple) {
               this.multiple = multiple;
               return this;
            }

            public AMQP.Basic.Nack.Builder multiple() {
               return this.multiple(true);
            }

            public AMQP.Basic.Nack.Builder requeue(boolean requeue) {
               this.requeue = requeue;
               return this;
            }

            public AMQP.Basic.Nack.Builder requeue() {
               return this.requeue(true);
            }

            public AMQP.Basic.Nack build() {
               return new AMQImpl.Basic.Nack(this.deliveryTag, this.multiple, this.requeue);
            }
         }
      }

      public interface Publish extends Method {
         int getTicket();

         String getExchange();

         String getRoutingKey();

         boolean getMandatory();

         boolean getImmediate();

         final class Builder {
            private int ticket = 0;
            private String exchange = "";
            private String routingKey = "";
            private boolean mandatory = false;
            private boolean immediate = false;

            public AMQP.Basic.Publish.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Basic.Publish.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Basic.Publish.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Basic.Publish.Builder mandatory(boolean mandatory) {
               this.mandatory = mandatory;
               return this;
            }

            public AMQP.Basic.Publish.Builder mandatory() {
               return this.mandatory(true);
            }

            public AMQP.Basic.Publish.Builder immediate(boolean immediate) {
               this.immediate = immediate;
               return this;
            }

            public AMQP.Basic.Publish.Builder immediate() {
               return this.immediate(true);
            }

            public AMQP.Basic.Publish build() {
               return new AMQImpl.Basic.Publish(this.ticket, this.exchange, this.routingKey, this.mandatory, this.immediate);
            }
         }
      }

      public interface Qos extends Method {
         int getPrefetchSize();

         int getPrefetchCount();

         boolean getGlobal();

         final class Builder {
            private int prefetchSize = 0;
            private int prefetchCount = 0;
            private boolean global = false;

            public AMQP.Basic.Qos.Builder prefetchSize(int prefetchSize) {
               this.prefetchSize = prefetchSize;
               return this;
            }

            public AMQP.Basic.Qos.Builder prefetchCount(int prefetchCount) {
               this.prefetchCount = prefetchCount;
               return this;
            }

            public AMQP.Basic.Qos.Builder global(boolean global) {
               this.global = global;
               return this;
            }

            public AMQP.Basic.Qos.Builder global() {
               return this.global(true);
            }

            public AMQP.Basic.Qos build() {
               return new AMQImpl.Basic.Qos(this.prefetchSize, this.prefetchCount, this.global);
            }
         }
      }

      public interface QosOk extends Method {
         final class Builder {
            public AMQP.Basic.QosOk build() {
               return new AMQImpl.Basic.QosOk();
            }
         }
      }

      public interface Recover extends Method {
         boolean getRequeue();

         final class Builder {
            private boolean requeue = false;

            public AMQP.Basic.Recover.Builder requeue(boolean requeue) {
               this.requeue = requeue;
               return this;
            }

            public AMQP.Basic.Recover.Builder requeue() {
               return this.requeue(true);
            }

            public AMQP.Basic.Recover build() {
               return new AMQImpl.Basic.Recover(this.requeue);
            }
         }
      }

      public interface RecoverAsync extends Method {
         boolean getRequeue();

         final class Builder {
            private boolean requeue = false;

            public AMQP.Basic.RecoverAsync.Builder requeue(boolean requeue) {
               this.requeue = requeue;
               return this;
            }

            public AMQP.Basic.RecoverAsync.Builder requeue() {
               return this.requeue(true);
            }

            public AMQP.Basic.RecoverAsync build() {
               return new AMQImpl.Basic.RecoverAsync(this.requeue);
            }
         }
      }

      public interface RecoverOk extends Method {
         final class Builder {
            public AMQP.Basic.RecoverOk build() {
               return new AMQImpl.Basic.RecoverOk();
            }
         }
      }

      public interface Reject extends Method {
         long getDeliveryTag();

         boolean getRequeue();

         final class Builder {
            private long deliveryTag;
            private boolean requeue = true;

            public AMQP.Basic.Reject.Builder deliveryTag(long deliveryTag) {
               this.deliveryTag = deliveryTag;
               return this;
            }

            public AMQP.Basic.Reject.Builder requeue(boolean requeue) {
               this.requeue = requeue;
               return this;
            }

            public AMQP.Basic.Reject.Builder requeue() {
               return this.requeue(true);
            }

            public AMQP.Basic.Reject build() {
               return new AMQImpl.Basic.Reject(this.deliveryTag, this.requeue);
            }
         }
      }

      public interface Return extends Method {
         int getReplyCode();

         String getReplyText();

         String getExchange();

         String getRoutingKey();

         final class Builder {
            private int replyCode;
            private String replyText = "";
            private String exchange;
            private String routingKey;

            public AMQP.Basic.Return.Builder replyCode(int replyCode) {
               this.replyCode = replyCode;
               return this;
            }

            public AMQP.Basic.Return.Builder replyText(String replyText) {
               this.replyText = replyText;
               return this;
            }

            public AMQP.Basic.Return.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Basic.Return.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Basic.Return build() {
               return new AMQImpl.Basic.Return(this.replyCode, this.replyText, this.exchange, this.routingKey);
            }
         }
      }
   }

   class BasicProperties extends AMQBasicProperties {
      private String contentType;
      private String contentEncoding;
      private Map<String, Object> headers;
      private Integer deliveryMode;
      private Integer priority;
      private String correlationId;
      private String replyTo;
      private String expiration;
      private String messageId;
      private Date timestamp;
      private String type;
      private String userId;
      private String appId;
      private String clusterId;

      public BasicProperties(
         String contentType,
         String contentEncoding,
         Map<String, Object> headers,
         Integer deliveryMode,
         Integer priority,
         String correlationId,
         String replyTo,
         String expiration,
         String messageId,
         Date timestamp,
         String type,
         String userId,
         String appId,
         String clusterId
      ) {
         this.contentType = contentType;
         this.contentEncoding = contentEncoding;
         this.headers = headers == null ? null : Collections.unmodifiableMap(new HashMap<>(headers));
         this.deliveryMode = deliveryMode;
         this.priority = priority;
         this.correlationId = correlationId;
         this.replyTo = replyTo;
         this.expiration = expiration;
         this.messageId = messageId;
         this.timestamp = timestamp;
         this.type = type;
         this.userId = userId;
         this.appId = appId;
         this.clusterId = clusterId;
      }

      public BasicProperties(DataInputStream in) throws IOException {
         super(in);
         ContentHeaderPropertyReader reader = new ContentHeaderPropertyReader(in);
         boolean contentType_present = reader.readPresence();
         boolean contentEncoding_present = reader.readPresence();
         boolean headers_present = reader.readPresence();
         boolean deliveryMode_present = reader.readPresence();
         boolean priority_present = reader.readPresence();
         boolean correlationId_present = reader.readPresence();
         boolean replyTo_present = reader.readPresence();
         boolean expiration_present = reader.readPresence();
         boolean messageId_present = reader.readPresence();
         boolean timestamp_present = reader.readPresence();
         boolean type_present = reader.readPresence();
         boolean userId_present = reader.readPresence();
         boolean appId_present = reader.readPresence();
         boolean clusterId_present = reader.readPresence();
         reader.finishPresence();
         this.contentType = contentType_present ? reader.readShortstr() : null;
         this.contentEncoding = contentEncoding_present ? reader.readShortstr() : null;
         this.headers = headers_present ? reader.readTable() : null;
         this.deliveryMode = deliveryMode_present ? reader.readOctet() : null;
         this.priority = priority_present ? reader.readOctet() : null;
         this.correlationId = correlationId_present ? reader.readShortstr() : null;
         this.replyTo = replyTo_present ? reader.readShortstr() : null;
         this.expiration = expiration_present ? reader.readShortstr() : null;
         this.messageId = messageId_present ? reader.readShortstr() : null;
         this.timestamp = timestamp_present ? reader.readTimestamp() : null;
         this.type = type_present ? reader.readShortstr() : null;
         this.userId = userId_present ? reader.readShortstr() : null;
         this.appId = appId_present ? reader.readShortstr() : null;
         this.clusterId = clusterId_present ? reader.readShortstr() : null;
      }

      public BasicProperties() {
      }

      @Override
      public int getClassId() {
         return 60;
      }

      @Override
      public String getClassName() {
         return "basic";
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (o != null && this.getClass() == o.getClass()) {
            AMQP.BasicProperties that = (AMQP.BasicProperties)o;
            if (this.contentType != null ? this.contentType.equals(that.contentType) : that.contentType == null) {
               if (this.contentEncoding != null ? this.contentEncoding.equals(that.contentEncoding) : that.contentEncoding == null) {
                  if (this.headers != null ? this.headers.equals(that.headers) : that.headers == null) {
                     if (this.deliveryMode != null ? this.deliveryMode.equals(that.deliveryMode) : that.deliveryMode == null) {
                        if (this.priority != null ? this.priority.equals(that.priority) : that.priority == null) {
                           if (this.correlationId != null ? this.correlationId.equals(that.correlationId) : that.correlationId == null) {
                              if (this.replyTo != null ? this.replyTo.equals(that.replyTo) : that.replyTo == null) {
                                 if (this.expiration != null ? this.expiration.equals(that.expiration) : that.expiration == null) {
                                    if (this.messageId != null ? this.messageId.equals(that.messageId) : that.messageId == null) {
                                       if (this.timestamp != null ? this.timestamp.equals(that.timestamp) : that.timestamp == null) {
                                          if (this.type != null ? this.type.equals(that.type) : that.type == null) {
                                             if (this.userId != null ? this.userId.equals(that.userId) : that.userId == null) {
                                                if (this.appId != null ? this.appId.equals(that.appId) : that.appId == null) {
                                                   return this.clusterId != null ? this.clusterId.equals(that.clusterId) : that.clusterId == null;
                                                } else {
                                                   return false;
                                                }
                                             } else {
                                                return false;
                                             }
                                          } else {
                                             return false;
                                          }
                                       } else {
                                          return false;
                                       }
                                    } else {
                                       return false;
                                    }
                                 } else {
                                    return false;
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = 0;
         result = 31 * result + (this.contentType != null ? this.contentType.hashCode() : 0);
         result = 31 * result + (this.contentEncoding != null ? this.contentEncoding.hashCode() : 0);
         result = 31 * result + (this.headers != null ? this.headers.hashCode() : 0);
         result = 31 * result + (this.deliveryMode != null ? this.deliveryMode.hashCode() : 0);
         result = 31 * result + (this.priority != null ? this.priority.hashCode() : 0);
         result = 31 * result + (this.correlationId != null ? this.correlationId.hashCode() : 0);
         result = 31 * result + (this.replyTo != null ? this.replyTo.hashCode() : 0);
         result = 31 * result + (this.expiration != null ? this.expiration.hashCode() : 0);
         result = 31 * result + (this.messageId != null ? this.messageId.hashCode() : 0);
         result = 31 * result + (this.timestamp != null ? this.timestamp.hashCode() : 0);
         result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
         result = 31 * result + (this.userId != null ? this.userId.hashCode() : 0);
         result = 31 * result + (this.appId != null ? this.appId.hashCode() : 0);
         return 31 * result + (this.clusterId != null ? this.clusterId.hashCode() : 0);
      }

      public AMQP.BasicProperties.Builder builder() {
         return new AMQP.BasicProperties.Builder()
            .contentType(this.contentType)
            .contentEncoding(this.contentEncoding)
            .headers(this.headers)
            .deliveryMode(this.deliveryMode)
            .priority(this.priority)
            .correlationId(this.correlationId)
            .replyTo(this.replyTo)
            .expiration(this.expiration)
            .messageId(this.messageId)
            .timestamp(this.timestamp)
            .type(this.type)
            .userId(this.userId)
            .appId(this.appId)
            .clusterId(this.clusterId);
      }

      @Override
      public String getContentType() {
         return this.contentType;
      }

      @Override
      public String getContentEncoding() {
         return this.contentEncoding;
      }

      @Override
      public Map<String, Object> getHeaders() {
         return this.headers;
      }

      @Override
      public Integer getDeliveryMode() {
         return this.deliveryMode;
      }

      @Override
      public Integer getPriority() {
         return this.priority;
      }

      @Override
      public String getCorrelationId() {
         return this.correlationId;
      }

      @Override
      public String getReplyTo() {
         return this.replyTo;
      }

      @Override
      public String getExpiration() {
         return this.expiration;
      }

      @Override
      public String getMessageId() {
         return this.messageId;
      }

      @Override
      public Date getTimestamp() {
         return this.timestamp;
      }

      @Override
      public String getType() {
         return this.type;
      }

      @Override
      public String getUserId() {
         return this.userId;
      }

      @Override
      public String getAppId() {
         return this.appId;
      }

      public String getClusterId() {
         return this.clusterId;
      }

      @Override
      public void writePropertiesTo(ContentHeaderPropertyWriter writer) throws IOException {
         writer.writePresence(this.contentType != null);
         writer.writePresence(this.contentEncoding != null);
         writer.writePresence(this.headers != null);
         writer.writePresence(this.deliveryMode != null);
         writer.writePresence(this.priority != null);
         writer.writePresence(this.correlationId != null);
         writer.writePresence(this.replyTo != null);
         writer.writePresence(this.expiration != null);
         writer.writePresence(this.messageId != null);
         writer.writePresence(this.timestamp != null);
         writer.writePresence(this.type != null);
         writer.writePresence(this.userId != null);
         writer.writePresence(this.appId != null);
         writer.writePresence(this.clusterId != null);
         writer.finishPresence();
         if (this.contentType != null) {
            writer.writeShortstr(this.contentType);
         }

         if (this.contentEncoding != null) {
            writer.writeShortstr(this.contentEncoding);
         }

         if (this.headers != null) {
            writer.writeTable(this.headers);
         }

         if (this.deliveryMode != null) {
            writer.writeOctet(this.deliveryMode);
         }

         if (this.priority != null) {
            writer.writeOctet(this.priority);
         }

         if (this.correlationId != null) {
            writer.writeShortstr(this.correlationId);
         }

         if (this.replyTo != null) {
            writer.writeShortstr(this.replyTo);
         }

         if (this.expiration != null) {
            writer.writeShortstr(this.expiration);
         }

         if (this.messageId != null) {
            writer.writeShortstr(this.messageId);
         }

         if (this.timestamp != null) {
            writer.writeTimestamp(this.timestamp);
         }

         if (this.type != null) {
            writer.writeShortstr(this.type);
         }

         if (this.userId != null) {
            writer.writeShortstr(this.userId);
         }

         if (this.appId != null) {
            writer.writeShortstr(this.appId);
         }

         if (this.clusterId != null) {
            writer.writeShortstr(this.clusterId);
         }
      }

      @Override
      public void appendPropertyDebugStringTo(StringBuilder acc) {
         acc.append("(content-type=")
            .append(this.contentType)
            .append(", content-encoding=")
            .append(this.contentEncoding)
            .append(", headers=")
            .append(this.headers)
            .append(", delivery-mode=")
            .append(this.deliveryMode)
            .append(", priority=")
            .append(this.priority)
            .append(", correlation-id=")
            .append(this.correlationId)
            .append(", reply-to=")
            .append(this.replyTo)
            .append(", expiration=")
            .append(this.expiration)
            .append(", message-id=")
            .append(this.messageId)
            .append(", timestamp=")
            .append(this.timestamp)
            .append(", type=")
            .append(this.type)
            .append(", user-id=")
            .append(this.userId)
            .append(", app-id=")
            .append(this.appId)
            .append(", cluster-id=")
            .append(this.clusterId)
            .append(")");
      }

      public static final class Builder {
         private String contentType;
         private String contentEncoding;
         private Map<String, Object> headers;
         private Integer deliveryMode;
         private Integer priority;
         private String correlationId;
         private String replyTo;
         private String expiration;
         private String messageId;
         private Date timestamp;
         private String type;
         private String userId;
         private String appId;
         private String clusterId;

         public AMQP.BasicProperties.Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
         }

         public AMQP.BasicProperties.Builder contentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
         }

         public AMQP.BasicProperties.Builder headers(Map<String, Object> headers) {
            this.headers = headers;
            return this;
         }

         public AMQP.BasicProperties.Builder deliveryMode(Integer deliveryMode) {
            this.deliveryMode = deliveryMode;
            return this;
         }

         public AMQP.BasicProperties.Builder priority(Integer priority) {
            this.priority = priority;
            return this;
         }

         public AMQP.BasicProperties.Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
         }

         public AMQP.BasicProperties.Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
         }

         public AMQP.BasicProperties.Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
         }

         public AMQP.BasicProperties.Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
         }

         public AMQP.BasicProperties.Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
         }

         public AMQP.BasicProperties.Builder type(String type) {
            this.type = type;
            return this;
         }

         public AMQP.BasicProperties.Builder userId(String userId) {
            this.userId = userId;
            return this;
         }

         public AMQP.BasicProperties.Builder appId(String appId) {
            this.appId = appId;
            return this;
         }

         public AMQP.BasicProperties.Builder clusterId(String clusterId) {
            this.clusterId = clusterId;
            return this;
         }

         public AMQP.BasicProperties build() {
            return new AMQP.BasicProperties(
               this.contentType,
               this.contentEncoding,
               this.headers,
               this.deliveryMode,
               this.priority,
               this.correlationId,
               this.replyTo,
               this.expiration,
               this.messageId,
               this.timestamp,
               this.type,
               this.userId,
               this.appId,
               this.clusterId
            );
         }
      }
   }

   class Channel {
      public interface Close extends Method {
         int getReplyCode();

         String getReplyText();

         int getClassId();

         int getMethodId();

         final class Builder {
            private int replyCode;
            private String replyText = "";
            private int classId;
            private int methodId;

            public AMQP.Channel.Close.Builder replyCode(int replyCode) {
               this.replyCode = replyCode;
               return this;
            }

            public AMQP.Channel.Close.Builder replyText(String replyText) {
               this.replyText = replyText;
               return this;
            }

            public AMQP.Channel.Close.Builder classId(int classId) {
               this.classId = classId;
               return this;
            }

            public AMQP.Channel.Close.Builder methodId(int methodId) {
               this.methodId = methodId;
               return this;
            }

            public AMQP.Channel.Close build() {
               return new AMQImpl.Channel.Close(this.replyCode, this.replyText, this.classId, this.methodId);
            }
         }
      }

      public interface CloseOk extends Method {
         final class Builder {
            public AMQP.Channel.CloseOk build() {
               return new AMQImpl.Channel.CloseOk();
            }
         }
      }

      public interface Flow extends Method {
         boolean getActive();

         final class Builder {
            private boolean active;

            public AMQP.Channel.Flow.Builder active(boolean active) {
               this.active = active;
               return this;
            }

            public AMQP.Channel.Flow.Builder active() {
               return this.active(true);
            }

            public AMQP.Channel.Flow build() {
               return new AMQImpl.Channel.Flow(this.active);
            }
         }
      }

      public interface FlowOk extends Method {
         boolean getActive();

         final class Builder {
            private boolean active;

            public AMQP.Channel.FlowOk.Builder active(boolean active) {
               this.active = active;
               return this;
            }

            public AMQP.Channel.FlowOk.Builder active() {
               return this.active(true);
            }

            public AMQP.Channel.FlowOk build() {
               return new AMQImpl.Channel.FlowOk(this.active);
            }
         }
      }

      public interface Open extends Method {
         String getOutOfBand();

         final class Builder {
            private String outOfBand = "";

            public AMQP.Channel.Open.Builder outOfBand(String outOfBand) {
               this.outOfBand = outOfBand;
               return this;
            }

            public AMQP.Channel.Open build() {
               return new AMQImpl.Channel.Open(this.outOfBand);
            }
         }
      }

      public interface OpenOk extends Method {
         LongString getChannelId();

         final class Builder {
            private LongString channelId = LongStringHelper.asLongString("");

            public AMQP.Channel.OpenOk.Builder channelId(LongString channelId) {
               this.channelId = channelId;
               return this;
            }

            public AMQP.Channel.OpenOk.Builder channelId(String channelId) {
               return this.channelId(LongStringHelper.asLongString(channelId));
            }

            public AMQP.Channel.OpenOk build() {
               return new AMQImpl.Channel.OpenOk(this.channelId);
            }
         }
      }
   }

   class Confirm {
      public interface Select extends Method {
         boolean getNowait();

         final class Builder {
            private boolean nowait = false;

            public AMQP.Confirm.Select.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Confirm.Select.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Confirm.Select build() {
               return new AMQImpl.Confirm.Select(this.nowait);
            }
         }
      }

      public interface SelectOk extends Method {
         final class Builder {
            public AMQP.Confirm.SelectOk build() {
               return new AMQImpl.Confirm.SelectOk();
            }
         }
      }
   }

   class Connection {
      public interface Blocked extends Method {
         String getReason();

         final class Builder {
            private String reason = "";

            public AMQP.Connection.Blocked.Builder reason(String reason) {
               this.reason = reason;
               return this;
            }

            public AMQP.Connection.Blocked build() {
               return new AMQImpl.Connection.Blocked(this.reason);
            }
         }
      }

      public interface Close extends Method {
         int getReplyCode();

         String getReplyText();

         int getClassId();

         int getMethodId();

         final class Builder {
            private int replyCode;
            private String replyText = "";
            private int classId;
            private int methodId;

            public AMQP.Connection.Close.Builder replyCode(int replyCode) {
               this.replyCode = replyCode;
               return this;
            }

            public AMQP.Connection.Close.Builder replyText(String replyText) {
               this.replyText = replyText;
               return this;
            }

            public AMQP.Connection.Close.Builder classId(int classId) {
               this.classId = classId;
               return this;
            }

            public AMQP.Connection.Close.Builder methodId(int methodId) {
               this.methodId = methodId;
               return this;
            }

            public AMQP.Connection.Close build() {
               return new AMQImpl.Connection.Close(this.replyCode, this.replyText, this.classId, this.methodId);
            }
         }
      }

      public interface CloseOk extends Method {
         final class Builder {
            public AMQP.Connection.CloseOk build() {
               return new AMQImpl.Connection.CloseOk();
            }
         }
      }

      public interface Open extends Method {
         String getVirtualHost();

         String getCapabilities();

         boolean getInsist();

         final class Builder {
            private String virtualHost = "/";
            private String capabilities = "";
            private boolean insist = false;

            public AMQP.Connection.Open.Builder virtualHost(String virtualHost) {
               this.virtualHost = virtualHost;
               return this;
            }

            public AMQP.Connection.Open.Builder capabilities(String capabilities) {
               this.capabilities = capabilities;
               return this;
            }

            public AMQP.Connection.Open.Builder insist(boolean insist) {
               this.insist = insist;
               return this;
            }

            public AMQP.Connection.Open.Builder insist() {
               return this.insist(true);
            }

            public AMQP.Connection.Open build() {
               return new AMQImpl.Connection.Open(this.virtualHost, this.capabilities, this.insist);
            }
         }
      }

      public interface OpenOk extends Method {
         String getKnownHosts();

         final class Builder {
            private String knownHosts = "";

            public AMQP.Connection.OpenOk.Builder knownHosts(String knownHosts) {
               this.knownHosts = knownHosts;
               return this;
            }

            public AMQP.Connection.OpenOk build() {
               return new AMQImpl.Connection.OpenOk(this.knownHosts);
            }
         }
      }

      public interface Secure extends Method {
         LongString getChallenge();

         final class Builder {
            private LongString challenge;

            public AMQP.Connection.Secure.Builder challenge(LongString challenge) {
               this.challenge = challenge;
               return this;
            }

            public AMQP.Connection.Secure.Builder challenge(String challenge) {
               return this.challenge(LongStringHelper.asLongString(challenge));
            }

            public AMQP.Connection.Secure build() {
               return new AMQImpl.Connection.Secure(this.challenge);
            }
         }
      }

      public interface SecureOk extends Method {
         LongString getResponse();

         final class Builder {
            private LongString response;

            public AMQP.Connection.SecureOk.Builder response(LongString response) {
               this.response = response;
               return this;
            }

            public AMQP.Connection.SecureOk.Builder response(String response) {
               return this.response(LongStringHelper.asLongString(response));
            }

            public AMQP.Connection.SecureOk build() {
               return new AMQImpl.Connection.SecureOk(this.response);
            }
         }
      }

      public interface Start extends Method {
         int getVersionMajor();

         int getVersionMinor();

         Map<String, Object> getServerProperties();

         LongString getMechanisms();

         LongString getLocales();

         final class Builder {
            private int versionMajor = 0;
            private int versionMinor = 9;
            private Map<String, Object> serverProperties;
            private LongString mechanisms = LongStringHelper.asLongString("PLAIN");
            private LongString locales = LongStringHelper.asLongString("en_US");

            public AMQP.Connection.Start.Builder versionMajor(int versionMajor) {
               this.versionMajor = versionMajor;
               return this;
            }

            public AMQP.Connection.Start.Builder versionMinor(int versionMinor) {
               this.versionMinor = versionMinor;
               return this;
            }

            public AMQP.Connection.Start.Builder serverProperties(Map<String, Object> serverProperties) {
               this.serverProperties = serverProperties;
               return this;
            }

            public AMQP.Connection.Start.Builder mechanisms(LongString mechanisms) {
               this.mechanisms = mechanisms;
               return this;
            }

            public AMQP.Connection.Start.Builder mechanisms(String mechanisms) {
               return this.mechanisms(LongStringHelper.asLongString(mechanisms));
            }

            public AMQP.Connection.Start.Builder locales(LongString locales) {
               this.locales = locales;
               return this;
            }

            public AMQP.Connection.Start.Builder locales(String locales) {
               return this.locales(LongStringHelper.asLongString(locales));
            }

            public AMQP.Connection.Start build() {
               return new AMQImpl.Connection.Start(this.versionMajor, this.versionMinor, this.serverProperties, this.mechanisms, this.locales);
            }
         }
      }

      public interface StartOk extends Method {
         Map<String, Object> getClientProperties();

         String getMechanism();

         LongString getResponse();

         String getLocale();

         final class Builder {
            private Map<String, Object> clientProperties;
            private String mechanism = "PLAIN";
            private LongString response;
            private String locale = "en_US";

            public AMQP.Connection.StartOk.Builder clientProperties(Map<String, Object> clientProperties) {
               this.clientProperties = clientProperties;
               return this;
            }

            public AMQP.Connection.StartOk.Builder mechanism(String mechanism) {
               this.mechanism = mechanism;
               return this;
            }

            public AMQP.Connection.StartOk.Builder response(LongString response) {
               this.response = response;
               return this;
            }

            public AMQP.Connection.StartOk.Builder response(String response) {
               return this.response(LongStringHelper.asLongString(response));
            }

            public AMQP.Connection.StartOk.Builder locale(String locale) {
               this.locale = locale;
               return this;
            }

            public AMQP.Connection.StartOk build() {
               return new AMQImpl.Connection.StartOk(this.clientProperties, this.mechanism, this.response, this.locale);
            }
         }
      }

      public interface Tune extends Method {
         int getChannelMax();

         int getFrameMax();

         int getHeartbeat();

         final class Builder {
            private int channelMax = 0;
            private int frameMax = 0;
            private int heartbeat = 0;

            public AMQP.Connection.Tune.Builder channelMax(int channelMax) {
               this.channelMax = channelMax;
               return this;
            }

            public AMQP.Connection.Tune.Builder frameMax(int frameMax) {
               this.frameMax = frameMax;
               return this;
            }

            public AMQP.Connection.Tune.Builder heartbeat(int heartbeat) {
               this.heartbeat = heartbeat;
               return this;
            }

            public AMQP.Connection.Tune build() {
               return new AMQImpl.Connection.Tune(this.channelMax, this.frameMax, this.heartbeat);
            }
         }
      }

      public interface TuneOk extends Method {
         int getChannelMax();

         int getFrameMax();

         int getHeartbeat();

         final class Builder {
            private int channelMax = 0;
            private int frameMax = 0;
            private int heartbeat = 0;

            public AMQP.Connection.TuneOk.Builder channelMax(int channelMax) {
               this.channelMax = channelMax;
               return this;
            }

            public AMQP.Connection.TuneOk.Builder frameMax(int frameMax) {
               this.frameMax = frameMax;
               return this;
            }

            public AMQP.Connection.TuneOk.Builder heartbeat(int heartbeat) {
               this.heartbeat = heartbeat;
               return this;
            }

            public AMQP.Connection.TuneOk build() {
               return new AMQImpl.Connection.TuneOk(this.channelMax, this.frameMax, this.heartbeat);
            }
         }
      }

      public interface Unblocked extends Method {
         final class Builder {
            public AMQP.Connection.Unblocked build() {
               return new AMQImpl.Connection.Unblocked();
            }
         }
      }

      public interface UpdateSecret extends Method {
         LongString getNewSecret();

         String getReason();

         final class Builder {
            private LongString newSecret;
            private String reason;

            public AMQP.Connection.UpdateSecret.Builder newSecret(LongString newSecret) {
               this.newSecret = newSecret;
               return this;
            }

            public AMQP.Connection.UpdateSecret.Builder newSecret(String newSecret) {
               return this.newSecret(LongStringHelper.asLongString(newSecret));
            }

            public AMQP.Connection.UpdateSecret.Builder reason(String reason) {
               this.reason = reason;
               return this;
            }

            public AMQP.Connection.UpdateSecret build() {
               return new AMQImpl.Connection.UpdateSecret(this.newSecret, this.reason);
            }
         }
      }

      public interface UpdateSecretOk extends Method {
         final class Builder {
            public AMQP.Connection.UpdateSecretOk build() {
               return new AMQImpl.Connection.UpdateSecretOk();
            }
         }
      }
   }

   class Exchange {
      public interface Bind extends Method {
         int getTicket();

         String getDestination();

         String getSource();

         String getRoutingKey();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String destination;
            private String source;
            private String routingKey = "";
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Exchange.Bind.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Exchange.Bind.Builder destination(String destination) {
               this.destination = destination;
               return this;
            }

            public AMQP.Exchange.Bind.Builder source(String source) {
               this.source = source;
               return this;
            }

            public AMQP.Exchange.Bind.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Exchange.Bind.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Exchange.Bind.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Exchange.Bind.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Exchange.Bind build() {
               return new AMQImpl.Exchange.Bind(this.ticket, this.destination, this.source, this.routingKey, this.nowait, this.arguments);
            }
         }
      }

      public interface BindOk extends Method {
         final class Builder {
            public AMQP.Exchange.BindOk build() {
               return new AMQImpl.Exchange.BindOk();
            }
         }
      }

      public interface Declare extends Method {
         int getTicket();

         String getExchange();

         String getType();

         boolean getPassive();

         boolean getDurable();

         boolean getAutoDelete();

         boolean getInternal();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String exchange;
            private String type = "direct";
            private boolean passive = false;
            private boolean durable = false;
            private boolean autoDelete = false;
            private boolean internal = false;
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Exchange.Declare.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Exchange.Declare.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Exchange.Declare.Builder type(String type) {
               this.type = type;
               return this;
            }

            public AMQP.Exchange.Declare.Builder passive(boolean passive) {
               this.passive = passive;
               return this;
            }

            public AMQP.Exchange.Declare.Builder passive() {
               return this.passive(true);
            }

            public AMQP.Exchange.Declare.Builder durable(boolean durable) {
               this.durable = durable;
               return this;
            }

            public AMQP.Exchange.Declare.Builder durable() {
               return this.durable(true);
            }

            public AMQP.Exchange.Declare.Builder autoDelete(boolean autoDelete) {
               this.autoDelete = autoDelete;
               return this;
            }

            public AMQP.Exchange.Declare.Builder autoDelete() {
               return this.autoDelete(true);
            }

            public AMQP.Exchange.Declare.Builder internal(boolean internal) {
               this.internal = internal;
               return this;
            }

            public AMQP.Exchange.Declare.Builder internal() {
               return this.internal(true);
            }

            public AMQP.Exchange.Declare.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Exchange.Declare.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Exchange.Declare.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Exchange.Declare build() {
               return new AMQImpl.Exchange.Declare(
                  this.ticket, this.exchange, this.type, this.passive, this.durable, this.autoDelete, this.internal, this.nowait, this.arguments
               );
            }
         }
      }

      public interface DeclareOk extends Method {
         final class Builder {
            public AMQP.Exchange.DeclareOk build() {
               return new AMQImpl.Exchange.DeclareOk();
            }
         }
      }

      public interface Delete extends Method {
         int getTicket();

         String getExchange();

         boolean getIfUnused();

         boolean getNowait();

         final class Builder {
            private int ticket = 0;
            private String exchange;
            private boolean ifUnused = false;
            private boolean nowait = false;

            public AMQP.Exchange.Delete.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Exchange.Delete.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Exchange.Delete.Builder ifUnused(boolean ifUnused) {
               this.ifUnused = ifUnused;
               return this;
            }

            public AMQP.Exchange.Delete.Builder ifUnused() {
               return this.ifUnused(true);
            }

            public AMQP.Exchange.Delete.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Exchange.Delete.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Exchange.Delete build() {
               return new AMQImpl.Exchange.Delete(this.ticket, this.exchange, this.ifUnused, this.nowait);
            }
         }
      }

      public interface DeleteOk extends Method {
         final class Builder {
            public AMQP.Exchange.DeleteOk build() {
               return new AMQImpl.Exchange.DeleteOk();
            }
         }
      }

      public interface Unbind extends Method {
         int getTicket();

         String getDestination();

         String getSource();

         String getRoutingKey();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String destination;
            private String source;
            private String routingKey = "";
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Exchange.Unbind.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Exchange.Unbind.Builder destination(String destination) {
               this.destination = destination;
               return this;
            }

            public AMQP.Exchange.Unbind.Builder source(String source) {
               this.source = source;
               return this;
            }

            public AMQP.Exchange.Unbind.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Exchange.Unbind.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Exchange.Unbind.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Exchange.Unbind.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Exchange.Unbind build() {
               return new AMQImpl.Exchange.Unbind(this.ticket, this.destination, this.source, this.routingKey, this.nowait, this.arguments);
            }
         }
      }

      public interface UnbindOk extends Method {
         final class Builder {
            public AMQP.Exchange.UnbindOk build() {
               return new AMQImpl.Exchange.UnbindOk();
            }
         }
      }
   }

   class PROTOCOL {
      public static final int MAJOR = 0;
      public static final int MINOR = 9;
      public static final int REVISION = 1;
      public static final int PORT = 5672;
   }

   class Queue {
      public interface Bind extends Method {
         int getTicket();

         String getQueue();

         String getExchange();

         String getRoutingKey();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private String exchange;
            private String routingKey = "";
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Queue.Bind.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Queue.Bind.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.Bind.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Queue.Bind.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Queue.Bind.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Queue.Bind.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Queue.Bind.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Queue.Bind build() {
               return new AMQImpl.Queue.Bind(this.ticket, this.queue, this.exchange, this.routingKey, this.nowait, this.arguments);
            }
         }
      }

      public interface BindOk extends Method {
         final class Builder {
            public AMQP.Queue.BindOk build() {
               return new AMQImpl.Queue.BindOk();
            }
         }
      }

      public interface Declare extends Method {
         int getTicket();

         String getQueue();

         boolean getPassive();

         boolean getDurable();

         boolean getExclusive();

         boolean getAutoDelete();

         boolean getNowait();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private boolean passive = false;
            private boolean durable = false;
            private boolean exclusive = false;
            private boolean autoDelete = false;
            private boolean nowait = false;
            private Map<String, Object> arguments = null;

            public AMQP.Queue.Declare.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Queue.Declare.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.Declare.Builder passive(boolean passive) {
               this.passive = passive;
               return this;
            }

            public AMQP.Queue.Declare.Builder passive() {
               return this.passive(true);
            }

            public AMQP.Queue.Declare.Builder durable(boolean durable) {
               this.durable = durable;
               return this;
            }

            public AMQP.Queue.Declare.Builder durable() {
               return this.durable(true);
            }

            public AMQP.Queue.Declare.Builder exclusive(boolean exclusive) {
               this.exclusive = exclusive;
               return this;
            }

            public AMQP.Queue.Declare.Builder exclusive() {
               return this.exclusive(true);
            }

            public AMQP.Queue.Declare.Builder autoDelete(boolean autoDelete) {
               this.autoDelete = autoDelete;
               return this;
            }

            public AMQP.Queue.Declare.Builder autoDelete() {
               return this.autoDelete(true);
            }

            public AMQP.Queue.Declare.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Queue.Declare.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Queue.Declare.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Queue.Declare build() {
               return new AMQImpl.Queue.Declare(
                  this.ticket, this.queue, this.passive, this.durable, this.exclusive, this.autoDelete, this.nowait, this.arguments
               );
            }
         }
      }

      public interface DeclareOk extends Method {
         String getQueue();

         int getMessageCount();

         int getConsumerCount();

         final class Builder {
            private String queue;
            private int messageCount;
            private int consumerCount;

            public AMQP.Queue.DeclareOk.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.DeclareOk.Builder messageCount(int messageCount) {
               this.messageCount = messageCount;
               return this;
            }

            public AMQP.Queue.DeclareOk.Builder consumerCount(int consumerCount) {
               this.consumerCount = consumerCount;
               return this;
            }

            public AMQP.Queue.DeclareOk build() {
               return new AMQImpl.Queue.DeclareOk(this.queue, this.messageCount, this.consumerCount);
            }
         }
      }

      public interface Delete extends Method {
         int getTicket();

         String getQueue();

         boolean getIfUnused();

         boolean getIfEmpty();

         boolean getNowait();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private boolean ifUnused = false;
            private boolean ifEmpty = false;
            private boolean nowait = false;

            public AMQP.Queue.Delete.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Queue.Delete.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.Delete.Builder ifUnused(boolean ifUnused) {
               this.ifUnused = ifUnused;
               return this;
            }

            public AMQP.Queue.Delete.Builder ifUnused() {
               return this.ifUnused(true);
            }

            public AMQP.Queue.Delete.Builder ifEmpty(boolean ifEmpty) {
               this.ifEmpty = ifEmpty;
               return this;
            }

            public AMQP.Queue.Delete.Builder ifEmpty() {
               return this.ifEmpty(true);
            }

            public AMQP.Queue.Delete.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Queue.Delete.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Queue.Delete build() {
               return new AMQImpl.Queue.Delete(this.ticket, this.queue, this.ifUnused, this.ifEmpty, this.nowait);
            }
         }
      }

      public interface DeleteOk extends Method {
         int getMessageCount();

         final class Builder {
            private int messageCount;

            public AMQP.Queue.DeleteOk.Builder messageCount(int messageCount) {
               this.messageCount = messageCount;
               return this;
            }

            public AMQP.Queue.DeleteOk build() {
               return new AMQImpl.Queue.DeleteOk(this.messageCount);
            }
         }
      }

      public interface Purge extends Method {
         int getTicket();

         String getQueue();

         boolean getNowait();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private boolean nowait = false;

            public AMQP.Queue.Purge.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Queue.Purge.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.Purge.Builder nowait(boolean nowait) {
               this.nowait = nowait;
               return this;
            }

            public AMQP.Queue.Purge.Builder nowait() {
               return this.nowait(true);
            }

            public AMQP.Queue.Purge build() {
               return new AMQImpl.Queue.Purge(this.ticket, this.queue, this.nowait);
            }
         }
      }

      public interface PurgeOk extends Method {
         int getMessageCount();

         final class Builder {
            private int messageCount;

            public AMQP.Queue.PurgeOk.Builder messageCount(int messageCount) {
               this.messageCount = messageCount;
               return this;
            }

            public AMQP.Queue.PurgeOk build() {
               return new AMQImpl.Queue.PurgeOk(this.messageCount);
            }
         }
      }

      public interface Unbind extends Method {
         int getTicket();

         String getQueue();

         String getExchange();

         String getRoutingKey();

         Map<String, Object> getArguments();

         final class Builder {
            private int ticket = 0;
            private String queue = "";
            private String exchange;
            private String routingKey = "";
            private Map<String, Object> arguments = null;

            public AMQP.Queue.Unbind.Builder ticket(int ticket) {
               this.ticket = ticket;
               return this;
            }

            public AMQP.Queue.Unbind.Builder queue(String queue) {
               this.queue = queue;
               return this;
            }

            public AMQP.Queue.Unbind.Builder exchange(String exchange) {
               this.exchange = exchange;
               return this;
            }

            public AMQP.Queue.Unbind.Builder routingKey(String routingKey) {
               this.routingKey = routingKey;
               return this;
            }

            public AMQP.Queue.Unbind.Builder arguments(Map<String, Object> arguments) {
               this.arguments = arguments;
               return this;
            }

            public AMQP.Queue.Unbind build() {
               return new AMQImpl.Queue.Unbind(this.ticket, this.queue, this.exchange, this.routingKey, this.arguments);
            }
         }
      }

      public interface UnbindOk extends Method {
         final class Builder {
            public AMQP.Queue.UnbindOk build() {
               return new AMQImpl.Queue.UnbindOk();
            }
         }
      }
   }

   class Tx {
      public interface Commit extends Method {
         final class Builder {
            public AMQP.Tx.Commit build() {
               return new AMQImpl.Tx.Commit();
            }
         }
      }

      public interface CommitOk extends Method {
         final class Builder {
            public AMQP.Tx.CommitOk build() {
               return new AMQImpl.Tx.CommitOk();
            }
         }
      }

      public interface Rollback extends Method {
         final class Builder {
            public AMQP.Tx.Rollback build() {
               return new AMQImpl.Tx.Rollback();
            }
         }
      }

      public interface RollbackOk extends Method {
         final class Builder {
            public AMQP.Tx.RollbackOk build() {
               return new AMQImpl.Tx.RollbackOk();
            }
         }
      }

      public interface Select extends Method {
         final class Builder {
            public AMQP.Tx.Select build() {
               return new AMQImpl.Tx.Select();
            }
         }
      }

      public interface SelectOk extends Method {
         final class Builder {
            public AMQP.Tx.SelectOk build() {
               return new AMQImpl.Tx.SelectOk();
            }
         }
      }
   }
}
