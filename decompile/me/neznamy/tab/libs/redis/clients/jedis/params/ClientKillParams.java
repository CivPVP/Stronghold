package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.ArrayList;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClientType;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class ClientKillParams implements IParams {
   private final ArrayList<KeyValue<Protocol.Keyword, Object>> params = new ArrayList<>();

   public static ClientKillParams clientKillParams() {
      return new ClientKillParams();
   }

   private ClientKillParams addParam(Protocol.Keyword key, Object value) {
      this.params.add(KeyValue.of(key, value));
      return this;
   }

   public ClientKillParams id(String clientId) {
      return this.addParam(Protocol.Keyword.ID, clientId);
   }

   public ClientKillParams id(byte[] clientId) {
      return this.addParam(Protocol.Keyword.ID, clientId);
   }

   public ClientKillParams type(ClientType type) {
      return this.addParam(Protocol.Keyword.TYPE, type);
   }

   public ClientKillParams addr(String ipPort) {
      return this.addParam(Protocol.Keyword.ADDR, ipPort);
   }

   public ClientKillParams addr(byte[] ipPort) {
      return this.addParam(Protocol.Keyword.ADDR, ipPort);
   }

   public ClientKillParams addr(String ip, int port) {
      return this.addParam(Protocol.Keyword.ADDR, ip + ':' + port);
   }

   public ClientKillParams skipMe(ClientKillParams.SkipMe skipMe) {
      return this.addParam(Protocol.Keyword.SKIPME, skipMe);
   }

   public ClientKillParams user(String username) {
      return this.addParam(Protocol.Keyword.USER, username);
   }

   public ClientKillParams laddr(String ipPort) {
      return this.addParam(Protocol.Keyword.LADDR, ipPort);
   }

   public ClientKillParams laddr(String ip, int port) {
      return this.addParam(Protocol.Keyword.LADDR, ip + ':' + port);
   }

   public ClientKillParams maxAge(long maxAge) {
      return this.addParam(Protocol.Keyword.MAXAGE, maxAge);
   }

   @Override
   public void addParams(CommandArguments args) {
      this.params.forEach(kv -> args.add(kv.getKey()).add(kv.getValue()));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ClientKillParams that = (ClientKillParams)o;
         return Objects.equals(this.params, that.params);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.params);
   }

   public enum SkipMe {
      YES,
      NO;
   }
}
