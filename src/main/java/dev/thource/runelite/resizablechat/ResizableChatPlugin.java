package dev.thource.runelite.resizablechat;

import com.google.inject.Provides;
import dev.thource.runelite.resizablechat.ui.UiManager;
import lombok.Getter;
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
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

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
    private RuneLiteConfig runeLiteConfig;
    @Inject
    public SpriteManager spriteManager;
    @Inject
    public UiManager uiManager;
    @Inject
    public ConfigManager configManager;
    protected boolean isDraggingV;
    protected boolean isDraggingH;
    protected Point dragStartPos = null;
    protected int dragStartValue;
    HotkeyListener hotkeyListener;
    @Getter
    private boolean inOverlayDragMode;
    private boolean dialogsNeedFixing;

    @Override
    protected void startUp() {

        spriteManager.addSpriteOverrides(CustomSprites.values());

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

    }

    @Override
    protected void shutDown() {
        spriteManager.removeSpriteOverrides(CustomSprites.values());
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
            return;
        }


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

    @Subscribe
    public void onVarbitChanged(VarbitChanged e) {
        uiManager.onVarbitChanged();
    }

    public boolean shouldReset() {
        Widget viewportChatboxParent = getViewportChatboxParent();
        Widget chatboxBackgroundLines = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);
        Widget chatboxFrame = client.getWidget(ComponentID.CHATBOX_FRAME);

        boolean state = viewportChatboxParent == null || (chatboxBackgroundLines == null || chatboxBackgroundLines.isHidden()) || chatboxFrame == null;

        uiManager.hideButtons(state);

        return state;
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
        dragStartValue = isVertical ? config.chatHeight() : config.chatWidth();
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
        if (shouldReset()) {
            return false;
        }
        if (config.resizingHandleMode() == ResizingHandleMode.NEVER
                || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !isInOverlayDragMode())) {
            return false;
        }

        Widget viewportChatboxParent = getViewportChatboxParent();
        return viewportChatboxParent != null;
    }

    public void checkResizing() {
        if (!shouldRender()) {
            return;
        }

        if (config.resizingHandleMode() == ResizingHandleMode.NEVER || (config.resizingHandleMode() == ResizingHandleMode.DRAG && !isInOverlayDragMode())) {
            return;
        }

        if (isDraggingV || isDraggingH) {
            Point mousePos = client.getMouseCanvasPosition();
            int newDimension;

            if (isDraggingV) {
                newDimension = Math.min(client.getCanvasHeight() - 24, Math.max(28, dragStartValue + (dragStartPos.getY() - mousePos.getY())));
                if (newDimension != config.chatHeight()) {
                    configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatHeight", newDimension);
                }
            } else if (isDraggingH) {
                newDimension = Math.min(client.getCanvasWidth() - 24, Math.max(519, dragStartValue + (mousePos.getX() - dragStartPos.getX())));
                if (newDimension != config.chatWidth()) {
                    configManager.setConfiguration(ResizableChatConfig.CONFIG_GROUP, "chatWidth", newDimension);
                }
            }
        }
    }


    private void resizeChatbox() {
        if (shouldReset()) {
            resetChatbox();
            return;
        }

        Widget viewportChatboxParent = getViewportChatboxParent();
        Widget chatboxFrame = client.getWidget(ComponentID.CHATBOX_FRAME);

        int oldHeight = viewportChatboxParent.getOriginalHeight();
        int newHeight = config.chatHeight();
        int oldWidth = viewportChatboxParent.getOriginalWidth();
        int newWidth = config.chatWidth();
        if (chatboxFrame == null || (oldHeight == newHeight + 23 && oldWidth == newWidth && chatboxFrame.getOriginalWidth() == newWidth)) {
            return;
        }

        Widget chatboxParent = client.getWidget(ComponentID.CHATBOX_PARENT);
        Widget chatboxBackground = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);
        Widget chatboxBackgroundLines = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES);

        if (chatboxParent == null || chatboxBackground == null || chatboxBackgroundLines == null) {
            return;
        }

        boolean isTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

        if (isTransparent) {
            viewportChatboxParent.setOriginalHeight(newHeight + 23 + 4);
            viewportChatboxParent.setOriginalWidth(newWidth + 8);
            chatboxBackground.setOriginalY(4);
            chatboxBackground.setOriginalWidth(8);
            chatboxBackground.setOriginalHeight(4);

            // Fixes issue with scrollbar being under side border
            chatboxBackgroundLines.setOriginalWidth(28);
            chatboxBackgroundLines.setOriginalHeight(16);
            chatboxBackgroundLines.setOriginalY(3);
        } else {
            viewportChatboxParent.setOriginalHeight(newHeight + 23 + 9);
            viewportChatboxParent.setOriginalWidth(newWidth + 4);
            chatboxBackground.setOriginalY(5);
            chatboxBackground.setOriginalWidth(0);
            chatboxBackground.setOriginalHeight(5);

            // Fixes issue with scrollbar being under side border
            chatboxBackgroundLines.setOriginalWidth(22);
            chatboxBackgroundLines.setOriginalHeight(16);
            chatboxBackgroundLines.setOriginalY(3);
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

    @Provides
    ResizableChatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ResizableChatConfig.class);
    }
}
