package dev.thource.runelite.resizablechat;

import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;


@Singleton
public class ChatboxSpriteOverlay extends HeightResizerOverlay {

    //TODO Get sprites from the cache so it follow things like resource packs and interface overlays

    static final BufferedImage bg = ImageUtil.loadImageResource(ResizableChatPlugin.class, "bg.png");

    static final BufferedImage left = ImageUtil.loadImageResource(ResizableChatPlugin.class, "left.png");
    static final BufferedImage right = ImageUtil.loadImageResource(ResizableChatPlugin.class, "right.png");
    static final BufferedImage bottom = ImageUtil.loadImageResource(ResizableChatPlugin.class, "bottom.png");
    static final BufferedImage top = ImageUtil.loadImageResource(ResizableChatPlugin.class, "top.png");
    static final BufferedImage bottom_left = ImageUtil.loadImageResource(ResizableChatPlugin.class, "bottom_left.png");
    static final BufferedImage bottom_right = ImageUtil.loadImageResource(ResizableChatPlugin.class, "bottom_right.png");

    static final BufferedImage top_left = ImageUtil.loadImageResource(ResizableChatPlugin.class, "top_left.png");
    static final BufferedImage top_right = ImageUtil.loadImageResource(ResizableChatPlugin.class, "top_right.png");

    ChatboxSpriteOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPriority(1f);
        setMovable(false);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1) {
            return null;
        }

        Widget chatboxBackground = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);

        // Draw the box
        int boxX = chatboxBackground.getCanvasLocation().getX();
        int boxY = chatboxBackground.getCanvasLocation().getY() - config.chatHeight() + 271;
        int boxWidth = config.chatWidth();
        int boxHeight = config.chatHeight();

        // Calculate corner images positions
        int topLeftX = boxX;
        int topLeftY = boxY;
        int topRightX = boxX + boxWidth - top_right.getWidth();
        int topRightY = boxY;
        int bottomLeftX = boxX;
        int bottomLeftY = boxY + boxHeight - bottom_left.getHeight();
        int bottomRightX = boxX + boxWidth - bottom_right.getWidth();
        int bottomRightY = boxY + boxHeight - bottom_right.getHeight();

        graphics.drawImage(bg,topLeftX,topLeftY,boxWidth,boxHeight,null);

        // Draw the corner images
        graphics.drawImage(top_left, topLeftX, topLeftY, null);
        graphics.drawImage(top_right, topRightX, topRightY, null);
        graphics.drawImage(bottom_left, bottomLeftX, bottomLeftY, null);
        graphics.drawImage(bottom_right, bottomRightX, bottomRightY, null);

        // Draw the side images
        for (int y = boxY + top_left.getHeight(); y < boxY + boxHeight - bottom_left.getHeight(); y += left.getHeight()) {
            graphics.drawImage(left, boxX + boxWidth - left.getWidth(), y, null);
        }

        for (int y = boxY + top_right.getHeight(); y < boxY + boxHeight - bottom_right.getHeight(); y += right.getHeight()) {
            graphics.drawImage(right, boxX, y, null);
        }

        // Draw the top and bottom images
        graphics.drawImage(top, boxX + top_left.getWidth(), boxY, boxX + boxWidth - top_right.getWidth(), boxY + top.getHeight(), 0, 0, top.getWidth(), top.getHeight(), null);
        graphics.drawImage(bottom, boxX + bottom_left.getWidth(), boxY + boxHeight - bottom_left.getHeight(), boxX + boxWidth - bottom_right.getWidth(), boxY + boxHeight, 0, 0, bottom.getWidth(), bottom.getHeight(), null);

        return new Dimension(config.chatWidth(),config.chatHeight());
    }

    @Override
    void startDragging() {
        isDragging = true;
        dragStartPos = client.getMouseCanvasPosition();
        dragStartValue = config.chatWidth();
    }
}
