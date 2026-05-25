package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.LongStringHelper;

public class JDKSaslConfig implements SaslConfig {
   private static final String[] DEFAULT_PREFERRED_MECHANISMS = new String[]{"PLAIN"};
   private final ConnectionFactory factory;
   private final List<String> mechanisms;
   private final CallbackHandler callbackHandler;

   public JDKSaslConfig(ConnectionFactory factory) {
      this(factory, DEFAULT_PREFERRED_MECHANISMS);
   }

   public JDKSaslConfig(ConnectionFactory factory, String[] mechanisms) {
      this.factory = factory;
      this.callbackHandler = new JDKSaslConfig.UsernamePasswordCallbackHandler(factory);
      this.mechanisms = Arrays.asList(mechanisms);
   }

   @Override
   public SaslMechanism getSaslMechanism(String[] serverMechanisms) {
      Set<String> server = new HashSet<>(Arrays.asList(serverMechanisms));

      for (String mechanism : this.mechanisms) {
         if (server.contains(mechanism)) {
            try {
               SaslClient saslClient = Sasl.createSaslClient(new String[]{mechanism}, null, "AMQP", this.factory.getHost(), null, this.callbackHandler);
               if (saslClient != null) {
                  return new JDKSaslConfig.JDKSaslMechanism(saslClient);
               }
            } catch (SaslException e) {
               throw new RuntimeException(e);
            }
         }
      }

      return null;
   }

   private class JDKSaslMechanism implements SaslMechanism {
      private final SaslClient client;

      public JDKSaslMechanism(SaslClient client) {
         this.client = client;
      }

      @Override
      public String getName() {
         return this.client.getMechanismName();
      }

      @Override
      public LongString handleChallenge(LongString challenge, String username, String password) {
         try {
            return LongStringHelper.asLongString(this.client.evaluateChallenge(challenge.getBytes()));
         } catch (SaslException e) {
            throw new RuntimeException(e);
         }
      }
   }

   private class UsernamePasswordCallbackHandler implements CallbackHandler {
      private final ConnectionFactory factory;

      public UsernamePasswordCallbackHandler(ConnectionFactory factory) {
         this.factory = factory;
      }

      @Override
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
         for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
               NameCallback nc = (NameCallback)callback;
               nc.setName(this.factory.getUsername());
            } else {
               if (!(callback instanceof PasswordCallback)) {
                  throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
               }

               PasswordCallback pc = (PasswordCallback)callback;
               pc.setPassword(this.factory.getPassword().toCharArray());
            }
         }
      }
   }
}
