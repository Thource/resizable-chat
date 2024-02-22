package dev.thource.runelite.resizablechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/** ResizableChatConfig manages the config for the plugin. */
@SuppressWarnings("SameReturnValue")
@ConfigGroup("resizableChat")
public interface ResizableChatConfig extends Config {

  String CONFIG_GROUP = "resizableChat";

  @Range(min = 142)
  @ConfigItem(
      keyName = "chatHeight",
      name = "Chat height",
      description = "How many pixels tall the chat should be."
  )
  default int chatHeight() {
    return 142;
  }

  @Range(min = 519)
  @ConfigItem(
      keyName = "chatWidth",
      name = "Chat width",
      description = "How many pixels wide the chat should be."
  )
  default int chatWidth() {
    return 519;
  }

  @ConfigItem(
      keyName = "resizingHandleMode",
      name = "Resizing handle mode",
      description = "Sets the behaviour for when the resizing handle should be shown."
          + "<br><br>"
          + "Always: Always show the handles<br>"
          + "Drag mode: Only show the handles when holding the drag hotkey (RuneLite config)<br>"
          + "Never: Never show the handles"
  )
  default ResizingHandleMode resizingHandleMode() {
    return ResizingHandleMode.ALWAYS;
  }
}
