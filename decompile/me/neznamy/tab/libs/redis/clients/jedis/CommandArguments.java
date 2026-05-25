package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Internal;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.args.RawableFactory;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.RediSearchUtil;

public class CommandArguments implements Iterable<Rawable> {
   private CommandKeyArgumentPreProcessor keyPreProc = null;
   private final ArrayList<Rawable> args;
   private List<Object> keys;
   private boolean blocking;

   private CommandArguments() {
      throw new InstantiationError();
   }

   public CommandArguments(ProtocolCommand command) {
      this.args = new ArrayList<>();
      this.args.add(command);
      this.keys = Collections.emptyList();
   }

   public ProtocolCommand getCommand() {
      return (ProtocolCommand)this.args.get(0);
   }

   @Experimental
   void setKeyArgumentPreProcessor(CommandKeyArgumentPreProcessor keyPreProcessor) {
      this.keyPreProc = keyPreProcessor;
   }

   public CommandArguments add(Rawable arg) {
      this.args.add(arg);
      return this;
   }

   public CommandArguments add(byte[] arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(boolean arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(int arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(long arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(double arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(String arg) {
      return this.add(RawableFactory.from(arg));
   }

   public CommandArguments add(Object arg) {
      if (arg == null) {
         throw new IllegalArgumentException("null is not a valid argument.");
      }

      if (arg instanceof Rawable) {
         this.args.add((Rawable)arg);
      } else if (arg instanceof byte[]) {
         this.args.add(RawableFactory.from((byte[])arg));
      } else if (arg instanceof Boolean) {
         this.args.add(RawableFactory.from((Boolean)arg));
      } else if (arg instanceof Integer) {
         this.args.add(RawableFactory.from((Integer)arg));
      } else if (arg instanceof Long) {
         this.args.add(RawableFactory.from((Long)arg));
      } else if (arg instanceof Double) {
         this.args.add(RawableFactory.from((Double)arg));
      } else if (arg instanceof float[]) {
         this.args.add(RawableFactory.from(RediSearchUtil.toByteArray((float[])arg)));
      } else if (arg instanceof String) {
         this.args.add(RawableFactory.from((String)arg));
      } else if (arg instanceof GeoCoordinate) {
         GeoCoordinate geo = (GeoCoordinate)arg;
         this.args.add(RawableFactory.from(geo.getLongitude() + "," + geo.getLatitude()));
      } else {
         this.args.add(RawableFactory.from(String.valueOf(arg)));
      }

      return this;
   }

   public CommandArguments addObjects(Object... args) {
      for (Object arg : args) {
         this.add(arg);
      }

      return this;
   }

   public CommandArguments addObjects(Collection args) {
      args.forEach(arg -> this.add(arg));
      return this;
   }

   public CommandArguments key(Object key) {
      if (this.keyPreProc != null) {
         key = this.keyPreProc.actualKey(key);
      }

      if (key instanceof Rawable) {
         Rawable raw = (Rawable)key;
         this.processKey(raw.getRaw());
         this.args.add(raw);
      } else if (key instanceof byte[]) {
         byte[] raw = (byte[])key;
         this.processKey(raw);
         this.args.add(RawableFactory.from(raw));
      } else {
         if (!(key instanceof String)) {
            throw new IllegalArgumentException("\"" + key.toString() + "\" is not a valid argument.");
         }

         String raw = (String)key;
         this.processKey(raw);
         this.args.add(RawableFactory.from(raw));
      }

      this.addKeyInKeys(key);
      return this;
   }

   private void addKeyInKeys(Object key) {
      if (this.keys.isEmpty()) {
         this.keys = Collections.singletonList(key);
      } else if (this.keys.size() == 1) {
         List oldKeys = this.keys;
         this.keys = new ArrayList<>();
         this.keys.addAll(oldKeys);
         this.keys.add(key);
      } else {
         this.keys.add(key);
      }
   }

   public final CommandArguments keys(Object... keys) {
      Arrays.stream(keys).forEach(this::key);
      return this;
   }

   public final CommandArguments keys(Collection keys) {
      keys.forEach(this::key);
      return this;
   }

   public final CommandArguments addParams(IParams params) {
      params.addParams(this);
      return this;
   }

   protected CommandArguments processKey(byte[] key) {
      return this;
   }

   protected final CommandArguments processKeys(byte[]... keys) {
      for (byte[] key : keys) {
         this.processKey(key);
      }

      return this;
   }

   protected CommandArguments processKey(String key) {
      return this;
   }

   protected final CommandArguments processKeys(String... keys) {
      for (String key : keys) {
         this.processKey(key);
      }

      return this;
   }

   public int size() {
      return this.args.size();
   }

   @Override
   public Iterator<Rawable> iterator() {
      return this.args.iterator();
   }

   @Internal
   public List<Object> getKeys() {
      return this.keys;
   }

   public boolean isBlocking() {
      return this.blocking;
   }

   public CommandArguments blocking() {
      this.blocking = true;
      return this;
   }
}
