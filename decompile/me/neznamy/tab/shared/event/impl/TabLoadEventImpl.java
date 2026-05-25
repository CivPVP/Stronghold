package me.neznamy.tab.shared.event.impl;

import lombok.Generated;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;

public class TabLoadEventImpl implements TabLoadEvent {
   private static final TabLoadEvent instance = new TabLoadEventImpl();

   @Generated
   private TabLoadEventImpl() {
   }

   @Generated
   public static TabLoadEvent getInstance() {
      return instance;
   }
}
