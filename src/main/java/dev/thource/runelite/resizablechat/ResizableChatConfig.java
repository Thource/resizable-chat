package dev.thource.runelite.resizablechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

/**
 * ResizableChatConfig manages the config for the plugin.
 */
@SuppressWarnings("SameReturnValue")
@ConfigGroup("resizableChat")
public interface ResizableChatConfig extends Config {

    String CONFIG_GROUP = "resizableChat";

    @Range(min = 28)
    @ConfigItem(
            keyName = "chatHeight",
            name = "Chat height",
            description = "How many pixels tall the chat should be."
    )
    default int chatHeight() {
        return 142;
    }

    @Range(min = 300)
    @ConfigItem(
            keyName = "chatWidth",
            name = "Chat width",
            description = "How many pixels wide the chat should be."
    )
    default int chatWidth() {
        return 519;
    }

    @ConfigItem(
            keyName = "alwaysShowResizingHandles",
            name = "Always show resizing handles",
            description = "Sets whether the handles should always be shown, or just when the keybind is held.",
            position = 4
    )
    default boolean alwaysShowResizingHandles() {
        return true;
    }

    @ConfigItem(
            keyName = "resizingHandleKeybind",
            name = "Resizing handle keybind",
            description = "Show the resizing handles when this keybind is held.",
            position = 5
    )
    default Keybind resizingHandleKeybind() {
        return Keybind.CTRL;
    }

}
