package dev.thource.runelite.resizablechat;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

@Singleton
public class ResizerOverlay extends Overlay {

  static final BufferedImage resizeIcon = ImageUtil.loadImageResource(ResizableChatPlugin.class,
      "resize.png");

  @Inject private Client client;
  @Inject private ResizableChatConfig config;
  @Inject private ConfigManager configManager;
  @Inject private ResizableChatPlugin plugin;

  private boolean isDragging;
  private Point dragStartPos = null;
  private int dragStartHeight;

  @Inject
  ResizerOverlay() {
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
    setPriority(2f);
    setMovable(false);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (config.resizingHandleMode() == ResizingHandleMode.NEVER
        || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !plugin.isInOverlayDragMode())
        || client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) != 1) {
      return null;
    }

    Widget viewportChatboxParent = plugin.getViewportChatboxParent();
    if (viewportChatboxParent == null) {
      return null;
    }

    graphics.drawImage(resizeIcon, 0, 0, null);

    Rectangle resizeIconBounds = getBounds();
    Point mousePos = client.getMouseCanvasPosition();
    if (isDragging) {
      int newHeight = Math.min(client.getCanvasHeight() - 24,
          Math.max(142, dragStartHeight + (dragStartPos.getY() - mousePos.getY())));

      if (newHeight != config.chatSize()) {
        configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatSize", newHeight);
      }
    }

    return resizeIconBounds.getSize();
  }

  void startDragging() {
    isDragging = true;
    dragStartPos = client.getMouseCanvasPosition();
    dragStartHeight = config.chatSize();
  }

  void stopDragging() {
    isDragging = false;
  }
}
