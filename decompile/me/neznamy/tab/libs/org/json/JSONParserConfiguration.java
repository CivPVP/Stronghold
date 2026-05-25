package me.neznamy.tab.libs.org.json;

public class JSONParserConfiguration extends ParserConfiguration {
   protected JSONParserConfiguration clone() {
      return new JSONParserConfiguration();
   }

   public JSONParserConfiguration withMaxNestingDepth(int maxNestingDepth) {
      return super.withMaxNestingDepth(maxNestingDepth);
   }
}
