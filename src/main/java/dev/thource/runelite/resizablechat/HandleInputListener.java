package dev.thource.runelite.resizablechat;

import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;

public class HandleInputListener implements KeyListener {

  private final ResizableChatConfig config;
  private final ResizableChatPlugin plugin;

  @Inject
  private HandleInputListener(ResizableChatPlugin plugin) {
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
  }

  @Override
  public void keyReleased(KeyEvent keyEvent) {
    if (config.resizingHandleKeybind().matches(keyEvent)) {
      plugin.setResizingKeybindPressed(false);
    }
  }
}
