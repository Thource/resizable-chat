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
public class HeightResizerOverlay extends Overlay {

  static final BufferedImage resizeIcon = ImageUtil.loadImageResource(ResizableChatPlugin.class,
      "resize.png");

  @Inject protected Client client;
  @Inject protected ResizableChatConfig config;
  @Inject protected ConfigManager configManager;
  @Inject protected ResizableChatPlugin plugin;

  protected boolean isDragging;
  protected Point dragStartPos = null;
  protected int dragStartValue;

  @Inject
  HeightResizerOverlay() {
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
    setPriority(2f);
    setMovable(false);
  }

  protected boolean shouldRender() {
    if (config.resizingHandleMode() == ResizingHandleMode.NEVER
        || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !plugin.isInOverlayDragMode())
        || client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) != 1) {
      return false;
    }

    Widget viewportChatboxParent = plugin.getViewportChatboxParent();
    return viewportChatboxParent != null;
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (!shouldRender()) {
      return null;
    }

    graphics.drawImage(resizeIcon, 0, 0, null);

    Rectangle resizeIconBounds = getBounds();
    Point mousePos = client.getMouseCanvasPosition();
    if (isDragging) {
      int newHeight = Math.min(client.getCanvasHeight() - 24,
          Math.max(142, dragStartValue + (dragStartPos.getY() - mousePos.getY())));

      if (newHeight != config.chatHeight()) {
        configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatHeight", newHeight);
      }
    }

    return resizeIconBounds.getSize();
  }

  void startDragging() {
    isDragging = true;
    dragStartPos = client.getMouseCanvasPosition();
    dragStartValue = config.chatHeight();
  }

  void stopDragging() {
    isDragging = false;
  }
}
