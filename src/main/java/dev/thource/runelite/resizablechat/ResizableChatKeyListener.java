package dev.thource.runelite.resizablechat;

import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;

public class ResizableChatKeyListener implements KeyListener {

  private final ResizableChatConfig config;
  private final ResizableChatPlugin plugin;

  @Inject
  private ResizableChatKeyListener(ResizableChatPlugin plugin) {
    this.plugin = plugin;
    this.config = plugin.getConfig();
  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {
    // not needed
  }

  @Override
  public void keyPressed(KeyEvent keyEvent) {
    if (config.resizingHandleKeybind().matches(keyEvent)) {
      plugin.setResizingKeybindPressed(true);
    }

    if (config.expandChatKeybind().matches(keyEvent)) {
      plugin.setExpandChatKeybindPressed(true);
    }
  }

  @Override
  public void keyReleased(KeyEvent keyEvent) {
    if (config.resizingHandleKeybind().matches(keyEvent)) {
      plugin.setResizingKeybindPressed(false);
    }

    if (config.expandChatKeybind().matches(keyEvent)) {
      plugin.setExpandChatKeybindPressed(false);
    }
  }
}
