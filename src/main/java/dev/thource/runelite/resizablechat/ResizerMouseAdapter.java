package dev.thource.runelite.resizablechat;

import java.awt.event.MouseEvent;
import javax.inject.Inject;
import net.runelite.client.input.MouseAdapter;

public class ResizerMouseAdapter extends MouseAdapter {

  @Inject private ResizerOverlay overlay;
  @Inject private ResizableChatConfig config;
  @Inject private ResizableChatPlugin plugin;

  @Inject
  ResizerMouseAdapter() {
  }

  @Override
  public MouseEvent mousePressed(MouseEvent mouseEvent) {
    if (config.resizingHandleMode() == ResizingHandleMode.NEVER
        || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !plugin.isInOverlayDragMode())
        || mouseEvent.getButton() != MouseEvent.BUTTON1
        || !overlay.getBounds().contains(mouseEvent.getPoint())) {
      return mouseEvent;
    }

    mouseEvent.consume();
    overlay.startDragging();
    return mouseEvent;
  }

  @Override
  public MouseEvent mouseReleased(MouseEvent mouseEvent) {
    overlay.stopDragging();
    return mouseEvent;
  }
}
