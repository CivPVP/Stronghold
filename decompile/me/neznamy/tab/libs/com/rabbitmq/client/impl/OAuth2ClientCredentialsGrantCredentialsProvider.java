package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.TrustEverythingTrustManager;

public class OAuth2ClientCredentialsGrantCredentialsProvider extends RefreshProtectedCredentialsProvider<OAuth2ClientCredentialsGrantCredentialsProvider.Token> {
   private static final String UTF_8_CHARSET = "UTF-8";
   private final String tokenEndpointUri;
   private final String clientId;
   private final String clientSecret;
   private final String grantType;
   private final Map<String, String> parameters;
   private final AtomicReference<Function<String, OAuth2ClientCredentialsGrantCredentialsProvider.Token>> tokenExtractor = new AtomicReference<>();
   private final String id;
   private final HostnameVerifier hostnameVerifier;
   private final SSLSocketFactory sslSocketFactory;
   private final Consumer<HttpURLConnection> connectionConfigurator;

   public OAuth2ClientCredentialsGrantCredentialsProvider(String tokenEndpointUri, String clientId, String clientSecret, String grantType) {
      this(tokenEndpointUri, clientId, clientSecret, grantType, new HashMap<>());
   }

   public OAuth2ClientCredentialsGrantCredentialsProvider(
      String tokenEndpointUri, String clientId, String clientSecret, String grantType, Map<String, String> parameters
   ) {
      this(tokenEndpointUri, clientId, clientSecret, grantType, parameters, null, null, null);
   }

   public OAuth2ClientCredentialsGrantCredentialsProvider(
      String tokenEndpointUri,
      String clientId,
      String clientSecret,
      String grantType,
      Map<String, String> parameters,
      Consumer<HttpURLConnection> connectionConfigurator
   ) {
      this(tokenEndpointUri, clientId, clientSecret, grantType, parameters, null, null, connectionConfigurator);
   }

   public OAuth2ClientCredentialsGrantCredentialsProvider(
      String tokenEndpointUri, String clientId, String clientSecret, String grantType, HostnameVerifier hostnameVerifier, SSLSocketFactory sslSocketFactory
   ) {
      this(tokenEndpointUri, clientId, clientSecret, grantType, new HashMap<>(), hostnameVerifier, sslSocketFactory, null);
   }

   public OAuth2ClientCredentialsGrantCredentialsProvider(
      String tokenEndpointUri,
      String clientId,
      String clientSecret,
      String grantType,
      Map<String, String> parameters,
      HostnameVerifier hostnameVerifier,
      SSLSocketFactory sslSocketFactory
   ) {
      this(tokenEndpointUri, clientId, clientSecret, grantType, parameters, hostnameVerifier, sslSocketFactory, null);
   }

   public OAuth2ClientCredentialsGrantCredentialsProvider(
      String tokenEndpointUri,
      String clientId,
      String clientSecret,
      String grantType,
      Map<String, String> parameters,
      HostnameVerifier hostnameVerifier,
      SSLSocketFactory sslSocketFactory,
      Consumer<HttpURLConnection> connectionConfigurator
   ) {
      this.tokenEndpointUri = tokenEndpointUri;
      this.clientId = clientId;
      this.clientSecret = clientSecret;
      this.grantType = grantType;
      this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
      this.hostnameVerifier = hostnameVerifier;
      this.sslSocketFactory = sslSocketFactory;
      this.connectionConfigurator = connectionConfigurator == null ? c -> {} : connectionConfigurator;
      this.id = UUID.randomUUID().toString();
   }

   private static StringBuilder encode(StringBuilder builder, String name, String value) throws UnsupportedEncodingException {
      if (value != null) {
         if (builder.length() > 0) {
            builder.append("&");
         }

         builder.append(encode(name, "UTF-8")).append("=").append(encode(value, "UTF-8"));
      }

      return builder;
   }

   private static String encode(String value, String charset) throws UnsupportedEncodingException {
      return URLEncoder.encode(value, charset);
   }

   private static String basicAuthentication(String username, String password) {
      String credentials = username + ":" + password;
      byte[] credentialsAsBytes = credentials.getBytes(StandardCharsets.ISO_8859_1);
      byte[] encodedBytes = Base64.getEncoder().encode(credentialsAsBytes);
      String encodedCredentials = new String(encodedBytes, StandardCharsets.ISO_8859_1);
      return "Basic " + encodedCredentials;
   }

   @Override
   public String getUsername() {
      return "";
   }

   protected String usernameFromToken(OAuth2ClientCredentialsGrantCredentialsProvider.Token token) {
      return "";
   }

   protected OAuth2ClientCredentialsGrantCredentialsProvider.Token parseToken(String response) {
      return this.tokenExtractor
         .updateAndGet(current -> current == null ? new OAuth2ClientCredentialsGrantCredentialsProvider.JacksonTokenLookup() : current)
         .apply(response);
   }

   protected OAuth2ClientCredentialsGrantCredentialsProvider.Token retrieveToken() {
      try {
         StringBuilder urlParameters = new StringBuilder();
         encode(urlParameters, "grant_type", this.grantType);

         for (Entry<String, String> parameter : this.parameters.entrySet()) {
            encode(urlParameters, parameter.getKey(), parameter.getValue());
         }

         byte[] postData = urlParameters.toString().getBytes(StandardCharsets.UTF_8);
         int postDataLength = postData.length;
         URL url = new URI(this.tokenEndpointUri).toURL();
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setDoOutput(true);
         conn.setInstanceFollowRedirects(false);
         conn.setRequestMethod("POST");
         conn.setRequestProperty("authorization", basicAuthentication(this.clientId, this.clientSecret));
         conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("charset", "UTF-8");
         conn.setRequestProperty("accept", "application/json");
         conn.setRequestProperty("content-length", Integer.toString(postDataLength));
         conn.setUseCaches(false);
         conn.setConnectTimeout(60000);
         conn.setReadTimeout(60000);
         this.configureConnection(conn);

         try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
         }

         this.checkResponseCode(conn.getResponseCode());
         this.checkContentType(conn.getHeaderField("content-type"));
         return this.parseToken(this.extractResponseBody(conn.getInputStream()));
      } catch (IOException | URISyntaxException e) {
         throw new OAuthTokenManagementException("Error while retrieving OAuth 2 token", e);
      }
   }

   protected void checkContentType(String headerField) throws OAuthTokenManagementException {
      if (headerField == null || !headerField.toLowerCase().contains("json")) {
         throw new OAuthTokenManagementException("HTTP request for token retrieval is not JSON: " + headerField);
      }
   }

   protected void checkResponseCode(int responseCode) throws OAuthTokenManagementException {
      if (responseCode != 200) {
         throw new OAuthTokenManagementException("HTTP request for token retrieval did not return 200 response code: " + responseCode);
      }
   }

   protected String extractResponseBody(InputStream inputStream) throws IOException {
      StringBuffer content = new StringBuffer();

      String inputLine;
      try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
         while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
         }
      }

      return content.toString();
   }

   protected String passwordFromToken(OAuth2ClientCredentialsGrantCredentialsProvider.Token token) {
      return token.getAccess();
   }

   protected Duration timeBeforeExpiration(OAuth2ClientCredentialsGrantCredentialsProvider.Token token) {
      return token.getTimeBeforeExpiration();
   }

   protected void configureConnection(HttpURLConnection connection) {
      this.connectionConfigurator.accept(connection);
      this.configureConnectionForHttps(connection);
   }

   protected void configureConnectionForHttps(HttpURLConnection connection) {
      if (connection instanceof HttpsURLConnection) {
         HttpsURLConnection securedConnection = (HttpsURLConnection)connection;
         if (this.hostnameVerifier != null) {
            securedConnection.setHostnameVerifier(this.hostnameVerifier);
         }

         if (this.sslSocketFactory != null) {
            securedConnection.setSSLSocketFactory(this.sslSocketFactory);
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         OAuth2ClientCredentialsGrantCredentialsProvider that = (OAuth2ClientCredentialsGrantCredentialsProvider)o;
         return this.id.equals(that.id);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   private static class JacksonTokenLookup implements Function<String, OAuth2ClientCredentialsGrantCredentialsProvider.Token> {
      private final ObjectMapper objectMapper = new ObjectMapper();

      private JacksonTokenLookup() {
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.Token apply(String response) {
         try {
            Map<?, ?> map = (Map<?, ?>)this.objectMapper.readValue(response, Map.class);
            int expiresIn = ((Number)map.get("expires_in")).intValue();
            Instant receivedAt = Instant.now();
            return new OAuth2ClientCredentialsGrantCredentialsProvider.Token(map.get("access_token").toString(), expiresIn, receivedAt);
         } catch (IOException e) {
            throw new OAuthTokenManagementException("Error while parsing OAuth 2 token", e);
         }
      }
   }

   public static class OAuth2ClientCredentialsGrantCredentialsProviderBuilder {
      private final Map<String, String> parameters = new HashMap<>();
      private String tokenEndpointUri;
      private String clientId;
      private String clientSecret;
      private String grantType = "client_credentials";
      private Consumer<HttpURLConnection> connectionConfigurator;
      private OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration tlsConfiguration = new OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration(
         this
      );

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder tokenEndpointUri(String tokenEndpointUri) {
         this.tokenEndpointUri = tokenEndpointUri;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder clientId(String clientId) {
         this.clientId = clientId;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder clientSecret(String clientSecret) {
         this.clientSecret = clientSecret;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder grantType(String grantType) {
         this.grantType = grantType;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder parameter(String name, String value) {
         this.parameters.put(name, value);
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder connectionConfigurator(
         Consumer<HttpURLConnection> connectionConfigurator
      ) {
         this.connectionConfigurator = connectionConfigurator;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration tls() {
         return this.tlsConfiguration;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider build() {
         return new OAuth2ClientCredentialsGrantCredentialsProvider(
            this.tokenEndpointUri,
            this.clientId,
            this.clientSecret,
            this.grantType,
            this.parameters,
            this.tlsConfiguration.hostnameVerifier,
            this.tlsConfiguration.sslSocketFactory(),
            this.connectionConfigurator
         );
      }
   }

   public static class TlsConfiguration {
      private final OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder builder;
      private HostnameVerifier hostnameVerifier;
      private SSLSocketFactory sslSocketFactory;
      private SSLContext sslContext;

      public TlsConfiguration(OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder builder) {
         this.builder = builder;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration hostnameVerifier(HostnameVerifier hostnameVerifier) {
         this.hostnameVerifier = hostnameVerifier;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration sslSocketFactory(SSLSocketFactory sslSocketFactory) {
         this.sslSocketFactory = sslSocketFactory;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration sslContext(SSLContext sslContext) {
         this.sslContext = sslContext;
         return this;
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.TlsConfiguration dev() {
         try {
            SSLContext sslContext = SSLContext.getInstance(
               ConnectionFactory.computeDefaultTlsProtocol(SSLContext.getDefault().getSupportedSSLParameters().getProtocols())
            );
            sslContext.init(null, new TrustManager[]{new TrustEverythingTrustManager()}, null);
            this.sslContext = sslContext;
            return this;
         } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new OAuthTokenManagementException("Error while creating TLS context for development configuration", e);
         }
      }

      public OAuth2ClientCredentialsGrantCredentialsProvider.OAuth2ClientCredentialsGrantCredentialsProviderBuilder builder() {
         return this.builder;
      }

      private SSLSocketFactory sslSocketFactory() {
         if (this.sslSocketFactory != null) {
            return this.sslSocketFactory;
         } else {
            return this.sslContext != null ? this.sslContext.getSocketFactory() : null;
         }
      }
   }

   public static class Token {
      private final String access;
      private final int expiresIn;
      private final Instant receivedAt;

      public Token(String access, int expiresIn, Instant receivedAt) {
         this.access = access;
         this.expiresIn = expiresIn;
         this.receivedAt = receivedAt;
      }

      public String getAccess() {
         return this.access;
      }

      public int getExpiresIn() {
         return this.expiresIn;
      }

      public Instant getReceivedAt() {
         return this.receivedAt;
      }

      public Duration getTimeBeforeExpiration() {
         Instant now = Instant.now();
         long age = this.receivedAt.until(now, ChronoUnit.SECONDS);
         return Duration.ofSeconds(this.expiresIn - age);
      }
   }
}
