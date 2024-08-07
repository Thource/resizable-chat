package dev.thource.runelite.resizablechat.ui.impl;

import com.google.inject.Singleton;
import dev.thource.runelite.resizablechat.CustomSprites;
import dev.thource.runelite.resizablechat.ResizableChatConfig;
import dev.thource.runelite.resizablechat.ResizableChatPlugin;
import dev.thource.runelite.resizablechat.ResizeType;
import dev.thource.runelite.resizablechat.ui.UI;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.ScriptEvent;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

/**
 * UI class for resize buttons.
 */
@Singleton
public class ResizingHandles extends UI {

    private final ResizeType type;
    private final Client client;
    private final ResizableChatConfig config;

    private final ResizableChatPlugin plugin;

    @Getter
    private Widget slider;
    private Widget tmp;
    private int buttonN = -1;
    private int buttonH = -1;
    @Getter @Setter private boolean isHidden;

    /**
     * Constructor for ResizeButtons.
     *
     * @param type   The type of resize (vertical or horizontal).
     * @param client The RuneLite client.
     * @param plugin The ResizableChatPlugin instance.
     */
    public ResizingHandles(ResizeType type, Client client, ResizableChatPlugin plugin) {
        this.type = type;
        this.client = client;
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    /**
     * Create resize buttons.
     *
     * @param parent The parent widget.
     */
    @Override
    public void create(Widget parent) {
        Widget chatbox = client.getWidget(ComponentID.CHATBOX_FRAME);
        tmp = chatbox.createChild(-1, WidgetType.GRAPHIC);

        tmp.setOnDragListener((JavaScriptCallback) this::onDrag);
        tmp.setOnDragCompleteListener((JavaScriptCallback) this::onDraggingFinished);
        tmp.setOnDialogAbortListener((JavaScriptCallback) this::onDraggingFinished);
        tmp.setDragParent(chatbox);
        Point location = getButtonLocation();
        tmp.setPos(location.getX(), location.getY());
        tmp.setHasListener(true);

        slider = chatbox.createChild(-1, WidgetType.GRAPHIC);
        setSpriteIds();
        slider.setAction(0, "Resize " + type.getFormatName());
        slider.setAction(1, "Reset " + type.getFormatName());
        slider.setOnOpListener((JavaScriptCallback) this::onButtonClicked);
        slider.setPos(location.getX(), location.getY());
        slider.setOnMouseRepeatListener((JavaScriptCallback) e -> onHover());
        slider.setOnMouseLeaveListener((JavaScriptCallback) e -> onLeave());
        slider.setHasListener(true);
    }

    @Override
    public void destroy(Widget parent) {
        Widget chatbox = client.getWidget(ComponentID.CHATBOX_FRAME);
        Widget[] children = chatbox.getChildren();

        children[tmp.getIndex()] = null;
        children[slider.getIndex()] = null;
    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onButtonClicked(ScriptEvent scriptEvent) {
        if (scriptEvent.getOp() != 2) {
            return;
        }

        String configKey = type == ResizeType.HORIZONTAL ? "chatWidth" : "chatHeight";
        int defaultValue = type == ResizeType.HORIZONTAL ? 519 : 142;
        plugin.configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, configKey, defaultValue);
        onResize();
    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onDraggingFinished(ScriptEvent scriptEvent) {
        if (slider.isHidden()) return;

        plugin.stopDragging();
        onResize();
    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onDrag(ScriptEvent scriptEvent) {
        if (slider.isHidden()) return;

        plugin.startDragging(type == ResizeType.VERTICAL);
    }

    /**
     * Responds to varbit changes.
     */
    @Override
    public void onVarbitChanged() {
        setSpriteIds();
    }

    /**
     * Adjusts the position of the slider after a resize.
     */
    public void onResize() {
        Point location = getButtonLocation();
        slider.setPos(location.getX(), location.getY());
        tmp.setPos(location.getX(), location.getY());
    }

    public Point getButtonLocation() {
        boolean isOpaque = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 0;

        Point location;
        if (type == ResizeType.VERTICAL) {
            if (isOpaque) {
                location = new Point(config.chatWidth() / 2 - 4, 0);
            } else {
                location = new Point(config.chatWidth() / 2 - 4, 3);
            }
        } else {
            if (isOpaque) {
                location = new Point(config.chatWidth() - 12, config.chatHeight() / 2 - 4);
            } else {
                location = new Point(config.chatWidth() - 8, config.chatHeight() / 2 - 4);
            }
        }
        return location;
    }

    private void onLeave() {
        slider.setSpriteId(buttonN);
    }

    private void onHover() {
        slider.setSpriteId(buttonH);
    }

    /**
     * Sets sprite IDs based on resize type and transparency.
     */
    private void setSpriteIds() {
        if (slider == null || tmp == null) {
            return;
        }

        boolean isOpaque = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 0;
        if (type == ResizeType.VERTICAL) {
            buttonN = isOpaque ? CustomSprites.RESIZE_V_BROWN.getSpriteId() : CustomSprites.RESIZE_V.getSpriteId();
            buttonH = isOpaque ? CustomSprites.RESIZE_V_BROWN_H.getSpriteId() : CustomSprites.RESIZE_V_H.getSpriteId();
            slider.setOriginalWidth(9);
            slider.setOriginalHeight(16);
            tmp.setOriginalWidth(9);
            tmp.setOriginalHeight(16);
        } else {
            buttonN = isOpaque ? CustomSprites.RESIZE_H_BROWN.getSpriteId() : CustomSprites.RESIZE_H.getSpriteId();
            buttonH = isOpaque ? CustomSprites.RESIZE_H_BROWN_H.getSpriteId() : CustomSprites.RESIZE_H_H.getSpriteId();
            slider.setOriginalWidth(16);
            slider.setOriginalHeight(9);
            tmp.setOriginalWidth(16);
            tmp.setOriginalHeight(9);
        }
        slider.setSpriteId(buttonN);
    }
}
