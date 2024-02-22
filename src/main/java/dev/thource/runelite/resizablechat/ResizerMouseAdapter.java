package dev.thource.runelite.resizablechat;

import java.awt.event.MouseEvent;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.MouseAdapter;

public class ResizerMouseAdapter extends MouseAdapter {

  @Inject private HeightResizerOverlay heightResizerOverlay;
  @Inject private WidthResizerOverlay widthResizerOverlay;
  @Inject private ResizableChatConfig config;
  @Inject private ResizableChatPlugin plugin;
  @Inject private ConfigManager configManager;

  @Inject
  ResizerMouseAdapter() {
  }

  @Override
  public MouseEvent mousePressed(MouseEvent mouseEvent) {
    if (config.resizingHandleMode() == ResizingHandleMode.NEVER
        || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !plugin.isInOverlayDragMode())
        || (mouseEvent.getButton() != MouseEvent.BUTTON1
        && mouseEvent.getButton() != MouseEvent.BUTTON3)) {
      return mouseEvent;
    }

    if (heightResizerOverlay.getBounds().contains(mouseEvent.getPoint())) {
      mouseEvent.consume();

      if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
        heightResizerOverlay.startDragging();
      } else {
        configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatHeight", 142);
      }
    } else if (widthResizerOverlay.getBounds().contains(mouseEvent.getPoint())) {
      mouseEvent.consume();

      if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
        widthResizerOverlay.startDragging();
      } else {
        configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatWidth", 519);
      }
    }

    return mouseEvent;
  }

  @Override
  public MouseEvent mouseReleased(MouseEvent mouseEvent) {
    heightResizerOverlay.stopDragging();
    widthResizerOverlay.stopDragging();

    return mouseEvent;
  }
}
