package dev.thource.runelite.resizablechat;

import com.google.inject.Provides;
import dev.thource.runelite.resizablechat.ui.UiManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ResizeableChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

/**
 * ResizableChatPlugin is a RuneLite plugin designed to allow the player to resize the chat when
 * playing in resizable mode.
 */
@Slf4j
@PluginDescriptor(name = "Resizable Chat", description = "Allows the chat to be resized when playing in resizable mode with transparent " + "chat.", tags = {"resize chat"})
public class ResizableChatPlugin extends Plugin {

    @Getter
    @Inject
    private Client client;
    @Getter
    @Inject
    private ClientThread clientThread;
    @Getter
    @Inject
    private ResizableChatConfig config;
    @Inject
    public SpriteManager spriteManager;
    @Inject
    public ResizableChatKeyListener resizableChatKeyListener;
    @Inject
    public KeyManager keyManager;
    @Inject
    public UiManager uiManager;
    @Inject
    public ConfigManager configManager;
    protected boolean isDraggingV;
    protected boolean isDraggingH;
    protected Point dragStartPos = null;
    protected int dragStartValue;
    @Getter
    private boolean inOverlayDragMode;
    private boolean dialogsNeedFixing;
    @Setter
    private boolean isExpandChatKeybindPressed;

    private static final int BUTTON_WIDTH = 56;
    private static final int REPORT_BUTTON_WIDTH = 79;
    private static final int BUTTON_SPACING = 3;
    private static final int TOTAL_BUTTON_SPACING = 48;

    @Override
    protected void startUp() {
        spriteManager.addSpriteOverrides(CustomSprites.values());
        keyManager.registerKeyListener(resizableChatKeyListener);
    }

    @Override
    protected void shutDown() {
        spriteManager.removeSpriteOverrides(CustomSprites.values());
        keyManager.unregisterKeyListener(resizableChatKeyListener);
        clientThread.invoke(() -> {
            uiManager.shutDown();
            resetChatbox();
            client.runScript(924);
        });
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGIN_SCREEN || e.getGameState() == GameState.HOPPING) {
            uiManager.setUiCreated(false);
        }
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
        }

        uiManager.hideResizingHandles(isChatHidden() || (!config.alwaysShowResizingHandles() && !uiManager.isHandleKeybindPressed()));
        uiManager.updateHiddenState();
        checkResizing();
        uiManager.create();
        resizeChatbox();
    }

    Widget getViewportChatboxParent() {
        Widget resizableModernChatboxParent = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_CHATBOX_PARENT);
        Widget resizableClassicChatboxParent = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_CHATBOX_PARENT);

        if (resizableModernChatboxParent != null && !resizableModernChatboxParent.isHidden()) {
            return resizableModernChatboxParent;
        }

        if (resizableClassicChatboxParent != null && !resizableClassicChatboxParent.isHidden()) {
            return resizableClassicChatboxParent;
        }

        return null;
    }

    public void resetChatbox() {
        Widget chatboxParent = client.getWidget(ComponentID.CHATBOX_PARENT);
        Widget chatboxBackground = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);
        Widget chatboxBackgroundLines = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);
        if (chatboxParent == null || chatboxParent.getOriginalHeight() == 0) {
            return;
        }

        Widget viewportChatboxParent = getViewportChatboxParent();

        if (viewportChatboxParent != null && !viewportChatboxParent.isHidden()) {
            viewportChatboxParent.setOriginalHeight(165);
            viewportChatboxParent.setOriginalWidth(519);
        }

        chatboxParent.setOriginalHeight(0);
        chatboxParent.setOriginalWidth(0);
        chatboxParent.setHeightMode(WidgetSizeMode.MINUS);
        chatboxParent.setWidthMode(WidgetSizeMode.MINUS);

        if (chatboxBackground != null) {
            chatboxBackground.setOriginalY(0);
            chatboxBackground.setOriginalWidth(0);
            chatboxBackground.setOriginalHeight(0);
        }

        if (chatboxBackgroundLines != null) {
            chatboxBackgroundLines.setOriginalWidth(0);
            chatboxBackgroundLines.setOriginalHeight(0);
            chatboxBackgroundLines.setOriginalY(0);
        }

        if (viewportChatboxParent != null && !viewportChatboxParent.isHidden()) {
            recursiveRevalidate(viewportChatboxParent);
        } else {
            recursiveRevalidate(chatboxParent);
        }

        // Resize the buttons

        Widget chatboxButtons = client.getWidget(ComponentID.CHATBOX_BUTTONS);
        if (chatboxButtons != null) {
            chatboxButtons.setOriginalWidth(519);
        }

        Widget chatboxButtonsContainer = client.getWidget(162, 3);
        if (chatboxButtonsContainer != null) {
            chatboxButtonsContainer.setOriginalWidth(519);
        }

        Widget reportButtonWidget = client.getWidget(162, 31);
        if (reportButtonWidget != null) {
            reportButtonWidget.setOriginalWidth(REPORT_BUTTON_WIDTH);
        }

        int[] buttonWidgets = new int[]{27, 23, 19, 15, 11, 7, 4};
        for (int i = 0; i < buttonWidgets.length; i++) {
            int widgetId = buttonWidgets[i];
            Widget widget = client.getWidget(162, widgetId);
            if (widget == null) {
                continue;
            }

            widget.setOriginalWidth(BUTTON_WIDTH);
            widget.setOriginalX(BUTTON_SPACING + (i * (BUTTON_WIDTH + BUTTON_SPACING * 2)) + REPORT_BUTTON_WIDTH + BUTTON_SPACING * 2);

            // Resize the button background
            Widget backgroundWidget = client.getWidget(162, widgetId + 1);
            if (backgroundWidget != null) {
                backgroundWidget.setOriginalWidth(BUTTON_WIDTH);
            }
        }


        client.refreshChat();

        uiManager.setHidden(true);

        // This solves a bug that occurs after chat is hidden
        dialogsNeedFixing = true;
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged e) {
        uiManager.onVarbitChanged();
    }

    public boolean isChatHidden() {
        Widget viewportChatboxParent = getViewportChatboxParent();
        Widget chatboxBackgroundLines = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);
        Widget chatboxFrame = client.getWidget(ComponentID.CHATBOX_FRAME);

        return viewportChatboxParent == null || (chatboxBackgroundLines == null || chatboxBackgroundLines.isHidden()) || chatboxFrame == null;
    }

    public void startDragging(boolean isVertical) {
        if (isVertical) {
            if (isDraggingV) {
                return;
            }

            isDraggingV = true;
        } else {
            if (isDraggingH) {
                return;
            }

            isDraggingH = true;
        }

        dragStartPos = client.getMouseCanvasPosition();
        dragStartValue = isVertical ? getTargetHeight() : config.chatWidth();
    }

    @Subscribe
    public void onResizeableChanged(ResizeableChanged e) {
        uiManager.onResizeableChanged();
    }

    public void stopDragging() {
        isDraggingV = false;
        isDraggingH = false;
    }

    protected boolean shouldRender() {
        if (anyOverlappingOverlayOpen() || isChatHidden() || (!config.alwaysShowResizingHandles() && !uiManager.isHandleKeybindPressed())) {
            return false;
        }

        Widget viewportChatboxParent = getViewportChatboxParent();
        return viewportChatboxParent != null;
    }

    private boolean anyOverlappingOverlayOpen() {
        int[] overlayWidgets = new int[]{16, 18};
        for (int widgetId : overlayWidgets) {
            Widget widget = client.getWidget(164, widgetId);
            if (widget != null) {
                if (widget.getNestedChildren().length > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getTargetHeight() {
        if (isExpandChatKeybindPressed) {
            return Math.min(client.getCanvasHeight() - 24, config.chatHeight() * 2);
        }

        return config.chatHeight();
    }

    public void checkResizing() {
        if (!shouldRender() || (!isDraggingV && !isDraggingH)) {
            return;
        }

        if (!config.alwaysShowResizingHandles() && !uiManager.isHandleKeybindPressed()) {
            stopDragging();
            return;
        }

        Point mousePos = client.getMouseCanvasPosition();
        int newDimension;

        if (isDraggingV) {
            newDimension = Math.min(client.getCanvasHeight() - 24, Math.max(28, dragStartValue + (dragStartPos.getY() - mousePos.getY())));
            if (newDimension != getTargetHeight()) {
              configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatHeight", newDimension);
            }
        } else if (isDraggingH) {
            newDimension = Math.min(client.getCanvasWidth() - 24, Math.max(300, dragStartValue + (mousePos.getX() - dragStartPos.getX())));
            if (newDimension != config.chatWidth()) {
              configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatWidth", newDimension);
            }
        }
    }

    private boolean isChatboxTransparent() {
        return client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
    }

    private void resizeChatbox() {
        if (anyOverlappingOverlayOpen() || isChatHidden()) {
            resetChatbox();
            return;
        }

        Widget viewportChatboxParent = getViewportChatboxParent();
        Widget chatboxFrame = client.getWidget(ComponentID.CHATBOX_FRAME);

        boolean isChatboxTransparent = isChatboxTransparent();
        int oldHeight = viewportChatboxParent.getOriginalHeight();
        int newHeight = getTargetHeight();
        int heightPadding = isChatboxTransparent ? 27 : 32;
        int oldWidth = viewportChatboxParent.getOriginalWidth();
        int newWidth = config.chatWidth();
        int widthPadding = isChatboxTransparent ? 8 : 4;
        if (chatboxFrame == null || (oldHeight == newHeight + heightPadding && oldWidth == newWidth + widthPadding && chatboxFrame.getOriginalWidth() == newWidth + widthPadding)) {
            return;
        }

        Widget chatboxParent = client.getWidget(ComponentID.CHATBOX_PARENT);
        Widget chatboxBackground = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);
        Widget chatboxBackgroundLines = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);
        Widget chatboxButtons = client.getWidget(ComponentID.CHATBOX_BUTTONS);

        if (chatboxParent == null || chatboxBackground == null || chatboxBackgroundLines == null || chatboxButtons == null) {
            return;
        }

        uiManager.setHidden(false);

        viewportChatboxParent.setOriginalHeight(newHeight + heightPadding);
        viewportChatboxParent.setOriginalWidth(newWidth + widthPadding);
        chatboxBackgroundLines.setOriginalHeight(16);
        chatboxBackgroundLines.setOriginalY(3);
        if (isChatboxTransparent) {
            chatboxBackground.setOriginalY(4);
            chatboxBackground.setOriginalWidth(8);
            chatboxBackground.setOriginalHeight(4);

            // Fixes issue with scrollbar being under side border
            chatboxBackgroundLines.setOriginalWidth(28);
        } else {
            chatboxBackground.setOriginalY(5);
            chatboxBackground.setOriginalWidth(0);
            chatboxBackground.setOriginalHeight(5);

            // Fixes issue with scrollbar being under side border
            chatboxBackgroundLines.setOriginalWidth(22);
        }

        chatboxParent.setOriginalHeight(viewportChatboxParent.getOriginalHeight());
        chatboxParent.setOriginalWidth(viewportChatboxParent.getOriginalWidth());
        chatboxParent.setHeightMode(WidgetSizeMode.ABSOLUTE);
        chatboxParent.setWidthMode(WidgetSizeMode.ABSOLUTE);

        chatboxFrame.setOriginalWidth(viewportChatboxParent.getOriginalWidth());

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

        // Resize the buttons

        chatboxButtons.setOriginalWidth(newWidth);
        Widget chatboxButtonsContainer = client.getWidget(162, 3);
        if (chatboxButtonsContainer != null) {
            chatboxButtonsContainer.setOriginalWidth(newWidth);
        }

        float chatboxButtonScale = (newWidth - TOTAL_BUTTON_SPACING) / (519f - TOTAL_BUTTON_SPACING);
        int reportButtonWidth = (int) Math.floor(REPORT_BUTTON_WIDTH * chatboxButtonScale);
        int buttonWidth = (int) Math.floor(BUTTON_WIDTH * chatboxButtonScale);

        Widget reportButtonWidget = client.getWidget(162, 31);
        if (reportButtonWidget != null) {
            reportButtonWidget.setOriginalWidth(reportButtonWidth);
        }

        int[] buttonWidgets = new int[]{27, 23, 19, 15, 11, 7, 4};
        for (int i = 0; i < buttonWidgets.length; i++) {
            int widgetId = buttonWidgets[i];
            Widget widget = client.getWidget(162, widgetId);
            if (widget == null) {
                continue;
            }

            widget.setOriginalWidth(buttonWidth);
            widget.setOriginalX(BUTTON_SPACING + (i * (buttonWidth + BUTTON_SPACING * 2)) + reportButtonWidth + BUTTON_SPACING * 2);

            // Resize the button background
            Widget backgroundWidget = client.getWidget(162, widgetId + 1);
            if (backgroundWidget == null) {
                continue;
            }

            if (config.stretchChatButtons() || buttonWidth < BUTTON_WIDTH) {
                backgroundWidget.setOriginalWidth(buttonWidth);
            } else {
                backgroundWidget.setOriginalWidth(BUTTON_WIDTH);
            }
        }

        // Adjust scroll when shrinking chat, so that the bottom chat line stays at the bottom of the box
        if (oldHeight != 165 && oldWidth != 519) {
            int heightDifference = newHeight - (oldHeight - heightPadding);
            client.setVarcIntValue(7, client.getVarcIntValue(7) - heightDifference);
        }

        recursiveRevalidate(viewportChatboxParent);
        client.refreshChat();

        uiManager.onChatBoxResized();
    }


    public void recursiveRevalidate(Widget widget) {
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

    void setResizingKeybindPressed(boolean pressed) {
        uiManager.setHandleKeybindPressed(pressed);
    }

    @Provides
    ResizableChatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ResizableChatConfig.class);
    }
}
