package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlsUtils {
   private static final Logger LOGGER = LoggerFactory.getLogger(TlsUtils.class);
   private static final List<String> KEY_USAGE = Collections.unmodifiableList(
      Arrays.asList(
         "digitalSignature", "nonRepudiation", "keyEncipherment", "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"
      )
   );
   private static final Map<String, String> EXTENDED_KEY_USAGE = Collections.unmodifiableMap(new HashMap<String, String>() {
      {
         this.put("1.3.6.1.5.5.7.3.1", "TLS Web server authentication");
         this.put("1.3.6.1.5.5.7.3.2", "TLS Web client authentication");
         this.put("1.3.6.1.5.5.7.3.3", "Signing of downloadable executable code");
         this.put("1.3.6.1.5.5.7.3.4", "E-mail protection");
         this.put("1.3.6.1.5.5.7.3.8", "Binding the hash of an object to a time from an agreed-upon time");
      }
   });
   private static String PARSING_ERROR = "<parsing-error>";
   private static final Map<String, BiFunction<byte[], X509Certificate, String>> EXTENSIONS = Collections.unmodifiableMap(
      new HashMap<String, BiFunction<byte[], X509Certificate, String>>() {
         {
            this.put("2.5.29.14", (v, c) -> "SubjectKeyIdentifier = " + TlsUtils.octetStringHexDump(v));
            this.put("2.5.29.15", (v, c) -> "KeyUsage = " + TlsUtils.keyUsageBitString(c.getKeyUsage(), v));
            this.put("2.5.29.16", (v, c) -> "PrivateKeyUsage = " + TlsUtils.hexDump(0, v));
            this.put("2.5.29.17", (v, c) -> {
               try {
                  return "SubjectAlternativeName = " + TlsUtils.sans(c, "/");
               } catch (CertificateParsingException e) {
                  return "SubjectAlternativeName = " + TlsUtils.PARSING_ERROR;
               }
            });
            this.put("2.5.29.18", (v, c) -> "IssuerAlternativeName = " + TlsUtils.hexDump(0, v));
            this.put("2.5.29.19", (v, c) -> "BasicConstraints = " + TlsUtils.basicConstraints(v));
            this.put("2.5.29.30", (v, c) -> "NameConstraints = " + TlsUtils.hexDump(0, v));
            this.put("2.5.29.33", (v, c) -> "PolicyMappings = " + TlsUtils.hexDump(0, v));
            this.put("2.5.29.35", (v, c) -> "AuthorityKeyIdentifier = " + TlsUtils.authorityKeyIdentifier(v));
            this.put("2.5.29.36", (v, c) -> "PolicyConstraints = " + TlsUtils.hexDump(0, v));
            this.put("2.5.29.37", (v, c) -> "ExtendedKeyUsage = " + TlsUtils.extendedKeyUsage(v, c));
         }
      }
   );

   public static void logPeerCertificateInfo(SSLSession session) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Certificate[] peerCertificates = session.getPeerCertificates();
            if (peerCertificates != null && peerCertificates.length > 0) {
               LOGGER.debug(peerCertificateInfo(peerCertificates[0], "Peer's leaf certificate"));

               for (int i = 1; i < peerCertificates.length; i++) {
                  LOGGER.debug(peerCertificateInfo(peerCertificates[i], "Peer's certificate chain entry"));
               }
            }
         } catch (Exception e) {
            LOGGER.debug("Error while logging peer certificate info: {}", e.getMessage());
         }
      }
   }

   public static String peerCertificateInfo(Certificate certificate, String prefix) {
      X509Certificate c = (X509Certificate)certificate;

      try {
         return String.format(
            "%s subject: %s, subject alternative names: %s, issuer: %s, not valid after: %s, X.509 usage extensions: %s",
            stripCRLF(prefix),
            stripCRLF(c.getSubjectX500Principal().getName()),
            stripCRLF(sans(c, ",")),
            stripCRLF(c.getIssuerX500Principal().getName()),
            c.getNotAfter(),
            stripCRLF(extensions(c))
         );
      } catch (Exception e) {
         return "Error while retrieving " + prefix + " certificate information";
      }
   }

   private static String sans(X509Certificate c, String separator) throws CertificateParsingException {
      return String.join(
         separator, Optional.ofNullable(c.getSubjectAlternativeNames()).orElse(new ArrayList<>()).stream().map(v -> v.toString()).collect(Collectors.toList())
      );
   }

   public static String extensionPrettyPrint(String oid, byte[] derOctetString, X509Certificate certificate) {
      try {
         return EXTENSIONS.getOrDefault(oid, (v, c) -> oid + " = " + hexDump(0, derOctetString)).apply(derOctetString, certificate);
      } catch (Exception e) {
         return oid + " = " + PARSING_ERROR;
      }
   }

   public static String stripCRLF(String value) {
      return value.replaceAll("\r", "").replaceAll("\n", "");
   }

   private static String extensions(X509Certificate certificate) {
      List<String> extensions = new ArrayList<>();

      for (String oid : certificate.getCriticalExtensionOIDs()) {
         extensions.add(extensionPrettyPrint(oid, certificate.getExtensionValue(oid), certificate) + " (critical)");
      }

      for (String oid : certificate.getNonCriticalExtensionOIDs()) {
         extensions.add(extensionPrettyPrint(oid, certificate.getExtensionValue(oid), certificate) + " (non-critical)");
      }

      return String.join(", ", extensions);
   }

   private static String octetStringHexDump(byte[] derOctetString) {
      return derOctetString.length > 4 && derOctetString[0] == 4 && derOctetString[2] == 4 ? hexDump(4, derOctetString) : hexDump(0, derOctetString);
   }

   private static String hexDump(int start, byte[] derOctetString) {
      List<String> hexs = new ArrayList<>();

      for (int i = start; i < derOctetString.length; i++) {
         hexs.add(String.format("%02X", derOctetString[i]));
      }

      return String.join(":", hexs);
   }

   private static String keyUsageBitString(boolean[] keyUsage, byte[] derOctetString) {
      if (keyUsage != null) {
         List<String> usage = new ArrayList<>();

         for (int i = 0; i < keyUsage.length; i++) {
            if (keyUsage[i]) {
               usage.add(KEY_USAGE.get(i));
            }
         }

         return String.join("/", usage);
      } else {
         return hexDump(0, derOctetString);
      }
   }

   private static String basicConstraints(byte[] derOctetString) {
      if (derOctetString.length == 4 && derOctetString[3] == 0) {
         return "CA:FALSE";
      } else {
         return derOctetString.length >= 7 && derOctetString[2] == 48 && derOctetString[4] == 1
            ? "CA:" + (derOctetString[6] == 0 ? "FALSE" : "TRUE")
            : hexDump(0, derOctetString);
      }
   }

   private static String authorityKeyIdentifier(byte[] derOctetString) {
      return derOctetString.length == 26 && derOctetString[0] == 4 ? "keyid:" + hexDump(6, derOctetString) : hexDump(0, derOctetString);
   }

   private static String extendedKeyUsage(byte[] derOctetString, X509Certificate certificate) {
      List<String> extendedKeyUsage = null;

      try {
         extendedKeyUsage = certificate.getExtendedKeyUsage();
         return extendedKeyUsage == null
            ? hexDump(0, derOctetString)
            : String.join("/", extendedKeyUsage.stream().map(oid -> EXTENDED_KEY_USAGE.getOrDefault(oid, oid)).collect(Collectors.toList()));
      } catch (CertificateParsingException e) {
         return PARSING_ERROR;
      }
   }
}
