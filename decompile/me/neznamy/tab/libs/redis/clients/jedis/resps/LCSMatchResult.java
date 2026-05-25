package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Collections;
import java.util.List;

public class LCSMatchResult {
   private String matchString;
   private List<LCSMatchResult.MatchedPosition> matches;
   private long len;

   public LCSMatchResult(String matchString) {
      this.matchString = matchString;
   }

   public LCSMatchResult(long len) {
      this.len = len;
   }

   public LCSMatchResult(List<LCSMatchResult.MatchedPosition> matches, long len) {
      this.matches = matches;
      this.len = len;
   }

   public LCSMatchResult(String matchString, List<LCSMatchResult.MatchedPosition> matches, long len) {
      this.matchString = matchString;
      this.matches = Collections.unmodifiableList(matches);
      this.len = len;
   }

   public String getMatchString() {
      return this.matchString;
   }

   public List<LCSMatchResult.MatchedPosition> getMatches() {
      return this.matches;
   }

   public long getLen() {
      return this.len;
   }

   public static class MatchedPosition {
      private final LCSMatchResult.Position a;
      private final LCSMatchResult.Position b;
      private final long matchLen;

      public MatchedPosition(LCSMatchResult.Position a, LCSMatchResult.Position b, long matchLen) {
         this.a = a;
         this.b = b;
         this.matchLen = matchLen;
      }

      public LCSMatchResult.Position getA() {
         return this.a;
      }

      public LCSMatchResult.Position getB() {
         return this.b;
      }

      public long getMatchLen() {
         return this.matchLen;
      }
   }

   public static class Position {
      private final long start;
      private final long end;

      public Position(long start, long end) {
         this.start = start;
         this.end = end;
      }

      public long getStart() {
         return this.start;
      }

      public long getEnd() {
         return this.end;
      }
   }
}
