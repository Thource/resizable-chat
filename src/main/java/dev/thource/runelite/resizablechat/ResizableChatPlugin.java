package dev.thource.runelite.resizablechat;

import com.google.inject.Provides;
import java.awt.Rectangle;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

/**
 * ResizableChatPlugin is a RuneLite plugin designed to allow the player to resize the chat when
 * playing in resizable mode.
 */
@Slf4j
@PluginDescriptor(
    name = "Resizable Chat",
    description = "Allows the chat to be resized when playing in resizable mode with transparent "
        + "chat.",
    tags = {"resize chat"}
)
public class ResizableChatPlugin extends Plugin {

  HotkeyListener hotkeyListener;
  @Getter @Inject private Client client;
  @Getter @Inject private ClientThread clientThread;
  @Getter @Inject private ResizableChatConfig config;
  @Inject private RuneLiteConfig runeLiteConfig;
  @Inject private OverlayManager overlayManager;
  @Inject private HeightResizerOverlay heightResizerOverlay;
  @Inject private WidthResizerOverlay widthResizerOverlay;
  @Inject private MouseManager mouseManager;
  @Inject private KeyManager keyManager;
  @Inject private ResizerMouseAdapter mouseAdapter;
  @Getter private boolean inOverlayDragMode;
  private boolean dialogsNeedFixing;

  @Override
  protected void startUp() {
    hotkeyListener = new HotkeyListener(runeLiteConfig::dragHotkey) {
      @Override
      public void hotkeyPressed() {
        inOverlayDragMode = true;
      }

      @Override
      public void hotkeyReleased() {
        if (inOverlayDragMode) {
          inOverlayDragMode = false;
        }
      }
    };

    overlayManager.add(heightResizerOverlay);
    overlayManager.add(widthResizerOverlay);
    mouseManager.registerMouseListener(mouseAdapter);
    keyManager.registerKeyListener(hotkeyListener);
  }


  @Override
  protected void shutDown() {
    overlayManager.remove(heightResizerOverlay);
    overlayManager.remove(widthResizerOverlay);
    mouseManager.unregisterMouseListener(mouseAdapter);
    keyManager.unregisterKeyListener(hotkeyListener);
  }

  @Subscribe
  public void onFocusChanged(FocusChanged focusChanged) {
    if (!focusChanged.isFocused()) {
      inOverlayDragMode = false;
    }
  }

  @Subscribe
  public void onClientTick(ClientTick tick) {
    if (dialogsNeedFixing) {
      client.runScript(924);
      dialogsNeedFixing = false;
      return;
    }

    Widget viewportChatboxParent = getViewportChatboxParent();

    if (viewportChatboxParent == null) {
      heightResizerOverlay.setBounds(new Rectangle());
      widthResizerOverlay.setBounds(new Rectangle());
    } else {
      // widget.isHidden needs to be called in the client thread, so we must setBounds instead
      Rectangle bounds = viewportChatboxParent.getBounds();
      heightResizerOverlay.setBounds(
          new Rectangle((int) bounds.getCenterX() - 4, Math.max(0, (int) bounds.getY() - 1),
              HeightResizerOverlay.resizeIcon.getWidth(),
              HeightResizerOverlay.resizeIcon.getHeight()));
      widthResizerOverlay.setBounds(
          new Rectangle((int) bounds.getMaxX() - 14, Math.max(0, (int) bounds.getCenterY() - 8),
              WidthResizerOverlay.resizeIcon.getWidth(),
              WidthResizerOverlay.resizeIcon.getHeight()));
    }

    resizeChatbox();
  }

  Widget getViewportChatboxParent() {
    Widget resizableModernChatboxParent = client.getWidget(
        ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_CHATBOX_PARENT);
    Widget resizableClassicChatboxParent = client.getWidget(
        ComponentID.RESIZABLE_VIEWPORT_CHATBOX_PARENT);

    if (resizableModernChatboxParent != null && !resizableModernChatboxParent.isHidden()) {
      return resizableModernChatboxParent;
    }

    if (resizableClassicChatboxParent != null && !resizableClassicChatboxParent.isHidden()) {
      return resizableClassicChatboxParent;
    }

    return null;
  }

  private void resetChatbox() {
    Widget chatboxParent = client.getWidget(ComponentID.CHATBOX_PARENT);
    if (chatboxParent == null || chatboxParent.getOriginalHeight() == 0) {
      return;
    }

    Widget viewportChatboxParent = getViewportChatboxParent();

    if (viewportChatboxParent != null && !viewportChatboxParent.isHidden()) {
      viewportChatboxParent.setOriginalHeight(165);
    }

    chatboxParent.setOriginalHeight(0);
    chatboxParent.setOriginalWidth(0);
    chatboxParent.setHeightMode(WidgetSizeMode.MINUS);
    chatboxParent.setWidthMode(WidgetSizeMode.MINUS);

    if (viewportChatboxParent != null && !viewportChatboxParent.isHidden()) {
      recursiveRevalidate(viewportChatboxParent);
    } else {
      recursiveRevalidate(chatboxParent);
    }

    client.refreshChat();

    // This solves a bug that occurs after chat is hidden
    dialogsNeedFixing = true;
  }

  private void resizeChatbox() {
    Widget viewportChatboxParent = getViewportChatboxParent();
    Widget chatboxBackgroundLines = client.getWidget(
        ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);
    Widget chatboxFrame = client.getWidget(ComponentID.CHATBOX_FRAME);

    if (viewportChatboxParent == null
        || client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) != 1
        || (chatboxBackgroundLines == null || chatboxBackgroundLines.isHidden())
        || chatboxFrame == null) {
      resetChatbox();
      return;
    }

    int oldHeight = viewportChatboxParent.getOriginalHeight();
    int newHeight = config.chatHeight();
    int oldWidth = viewportChatboxParent.getOriginalWidth();
    int newWidth = config.chatWidth();
    if (oldHeight == newHeight + 23 && oldWidth == newWidth
        && chatboxFrame.getOriginalWidth() == newWidth) {
      return;
    }

    Widget chatboxParent = client.getWidget(ComponentID.CHATBOX_PARENT);
    Widget chatboxBackground = client.getWidget(
        ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);

    if (chatboxParent == null || chatboxBackground == null) {
      return;
    }

    viewportChatboxParent.setOriginalHeight(newHeight + 23);
    viewportChatboxParent.setOriginalWidth(newWidth);

    chatboxFrame.setOriginalWidth(newWidth);

    chatboxParent.setOriginalHeight(viewportChatboxParent.getOriginalHeight());
    chatboxParent.setOriginalWidth(viewportChatboxParent.getOriginalWidth());
    chatboxParent.setHeightMode(WidgetSizeMode.ABSOLUTE);
    chatboxParent.setWidthMode(WidgetSizeMode.ABSOLUTE);

    Widget[] bgLines = chatboxBackground.getChildren();
    if (bgLines != null) {
      for (int i = 0; i < bgLines.length; i++) {
        Widget bg = chatboxBackground.getChildren()[i];
        int lineHeight = newHeight / 20;

        if (i == bgLines.length - 1) {
          bg.setOriginalHeight(newHeight - (lineHeight * 19)); // fill the rest of the space
        } else {
          bg.setOriginalHeight(lineHeight);
        }
        bg.setOriginalY(i * lineHeight);
      }
    }

    recursiveRevalidate(viewportChatboxParent);
    client.refreshChat();
  }

  private void recursiveRevalidate(Widget widget) {
    if (widget == null) {
      return;
    }

    widget.revalidate();

    Widget[] staticChildren = widget.getStaticChildren();
    if (staticChildren != null) {
      for (Widget child : staticChildren) {
        recursiveRevalidate(child);
      }
    }

    Widget[] dynamicChildren = widget.getDynamicChildren();
    if (dynamicChildren != null) {
      for (Widget child : dynamicChildren) {
        recursiveRevalidate(child);
      }
    }

    Widget[] nestedChildren = widget.getNestedChildren();
    if (nestedChildren != null) {
      for (Widget child : nestedChildren) {
        recursiveRevalidate(child);
      }
    }
  }

  @Provides
  ResizableChatConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ResizableChatConfig.class);
  }
}
