package dev.thource.runelite.resizablechat;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Singleton;

import net.runelite.api.Point;
import net.runelite.client.util.ImageUtil;

@Singleton
public class WidthResizerOverlay extends HeightResizerOverlay {

    static final BufferedImage resizeIcon = ImageUtil.loadImageResource(ResizableChatPlugin.class,
            "resize-90.png");

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!shouldRender()) {
            return null;
        }

        graphics.drawImage(resizeIcon, 0, 0, null);

        Rectangle resizeIconBounds = getBounds();
        Point mousePos = client.getMouseCanvasPosition();
        if (isDragging) {
            int newWidth = Math.min(client.getCanvasWidth() - 24,
                    Math.max(519, dragStartValue + (mousePos.getX() - dragStartPos.getX())));

            if (newWidth != config.chatWidth()) {
                configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatWidth", newWidth);
            }
        }

        return resizeIconBounds.getSize();
    }

    @Override
    void startDragging() {
        isDragging = true;
        dragStartPos = client.getMouseCanvasPosition();
        dragStartValue = config.chatWidth();
    }
}
