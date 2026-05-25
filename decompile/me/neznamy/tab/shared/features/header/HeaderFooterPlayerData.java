package me.neznamy.tab.shared.features.header;

import java.util.IdentityHashMap;
import java.util.Map;
import me.neznamy.tab.shared.Property;
import org.jetbrains.annotations.Nullable;

public class HeaderFooterPlayerData {
   @Nullable
   public Property forcedHeader;
   @Nullable
   public Property forcedFooter;
   @Nullable
   public HeaderFooterDesign activeDesign;
   public final Map<HeaderFooterDesign, Property> headerProperties = new IdentityHashMap<>();
   public final Map<HeaderFooterDesign, Property> footerProperties = new IdentityHashMap<>();
}
