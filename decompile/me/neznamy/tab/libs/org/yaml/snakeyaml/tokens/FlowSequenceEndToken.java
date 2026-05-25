package me.neznamy.tab.libs.org.yaml.snakeyaml.tokens;

import me.neznamy.tab.libs.org.yaml.snakeyaml.error.Mark;

public final class FlowSequenceEndToken extends Token {
   public FlowSequenceEndToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowSequenceEnd;
   }
}
