package me.neznamy.tab.libs.org.yaml.snakeyaml.scanner;

import me.neznamy.tab.libs.org.yaml.snakeyaml.tokens.Token;

public interface Scanner {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token getToken();
}
