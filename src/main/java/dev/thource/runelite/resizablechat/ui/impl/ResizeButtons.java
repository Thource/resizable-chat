package dev.thource.runelite.resizablechat.ui.impl;

import com.google.inject.Singleton;
import dev.thource.runelite.resizablechat.CustomSprites;
import dev.thource.runelite.resizablechat.ResizableChatConfig;
import dev.thource.runelite.resizablechat.ResizableChatPlugin;
import dev.thource.runelite.resizablechat.ResizeType;
import dev.thource.runelite.resizablechat.ui.UI;
import net.runelite.api.*;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.config.ConfigManager;

/**
 * UI class for resize buttons.
 */
@Singleton
public class ResizeButtons extends UI {

    private final ResizeType type;
    private final Client client;
    private final ResizableChatConfig config;
    private final ConfigManager configManager;

    private final ResizableChatPlugin plugin;

    public Widget slider;
    public Widget tmp;
    private int buttonN = -1;
    private int buttonH = -1;

    /**
     * Constructor for ResizeButtons.
     *
     * @param type    The type of resize (vertical or horizontal).
     * @param client  The RuneLite client.
     * @param plugin  The ResizableChatPlugin instance.
     */
    public ResizeButtons(ResizeType type, Client client, ResizableChatPlugin plugin) {
        this.type = type;
        this.client = client;
        this.config = plugin.getConfig();
        this.configManager = plugin.configManager;
        this.plugin = plugin;
    }

    /**
     * Create resize buttons.
     *
     * @param parent The parent widget.
     */
    @Override
    public void create(Widget parent) {

        tmp = client.getWidget(WidgetInfo.CHATBOX).createChild(-1, WidgetType.GRAPHIC);

        tmp.setOnDragListener((JavaScriptCallback) this::onOnDrag);
        tmp.setOnDragCompleteListener((JavaScriptCallback) this::onOnDraggingFinshed);
        tmp.setOnDialogAbortListener((JavaScriptCallback) this::onOnDraggingFinshed);
        tmp.setDragParent(client.getWidget(WidgetInfo.CHATBOX));
        Point location = getButtonLocation();
        tmp.setPos(location.getX(),location.getY());
        tmp.setHasListener(true);

        slider = client.getWidget(WidgetInfo.CHATBOX).createChild(-1, WidgetType.GRAPHIC);
        setSpriteIds();
        slider.setAction(0, "Resize " + type.getFormatName());
        slider.setAction(1, "Reset " + type.getFormatName());
        slider.setOnOpListener((JavaScriptCallback) this::onButtonClicked);
        slider.setPos(location.getX(),location.getY());
        slider.setOnMouseRepeatListener((JavaScriptCallback) e -> onHover());
        slider.setOnMouseLeaveListener((JavaScriptCallback) e -> onLeave());
        slider.setHasListener(true);

    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onButtonClicked(ScriptEvent scriptEvent) {
        int option = scriptEvent.getOp() - 1;
        if (option == 1) {
            String configKey = type == ResizeType.HORIZONTAL ? "chatWidth" : "chatHeight";
            int defaultValue = type == ResizeType.HORIZONTAL ? 519 : 142;
            plugin.configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, configKey, defaultValue);
        }
    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onOnDraggingFinshed(ScriptEvent scriptEvent) {
        if (slider.isHidden()) return;
        plugin.stopDragging();
    }

    /**
     * Handles button click events.
     *
     * @param scriptEvent The script event.
     */
    public void onOnDrag(ScriptEvent scriptEvent) {
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
        Point location;
        if (type == ResizeType.VERTICAL) {
            location = new Point(config.chatWidth() / 2 - (9 / 2), 0);
        } else {
            location = new Point(config.chatWidth() - (16 / 2), config.chatHeight() / 2 - (9 / 2));
        }
        return location;
    }

    private void onLeave()
    {
        slider.setSpriteId(buttonN);
    }

    private void onHover()
    {
        slider.setSpriteId(buttonH);
    }

    /**
     * Sets sprite IDs based on resize type and transparency.
     */
    private void setSpriteIds() {
        boolean isTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 0;
        if (type == ResizeType.VERTICAL) {
            buttonN = isTransparent ? CustomSprites.RESIZE_V_BROWN.getSpriteId() : CustomSprites.RESIZE_V.getSpriteId();
            buttonH = isTransparent ? CustomSprites.RESIZE_V_BROWN_H.getSpriteId() : CustomSprites.RESIZE_V_H.getSpriteId();
            slider.setOriginalWidth(9);
            slider.setOriginalHeight(16);
            tmp.setOriginalWidth(9);
            tmp.setOriginalHeight(16);
        } else {
            buttonN = isTransparent ? CustomSprites.RESIZE_H_BROWN.getSpriteId() : CustomSprites.RESIZE_H.getSpriteId();
            buttonH = isTransparent ? CustomSprites.RESIZE_H_BROWN_H.getSpriteId() : CustomSprites.RESIZE_H_H.getSpriteId();
            slider.setOriginalWidth(16);
            slider.setOriginalHeight(9);
            tmp.setOriginalWidth(16);
            tmp.setOriginalHeight(9);
        }
        slider.setSpriteId(buttonN);
    }
}
