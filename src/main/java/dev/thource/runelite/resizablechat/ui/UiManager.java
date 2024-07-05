package dev.thource.runelite.resizablechat.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.thource.runelite.resizablechat.ResizableChatPlugin;
import dev.thource.runelite.resizablechat.ResizeType;
import dev.thource.runelite.resizablechat.ui.impl.ChatBoxBackground;
import dev.thource.runelite.resizablechat.ui.impl.ResizingHandles;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;

import java.util.ArrayList;

@Singleton
@Slf4j
public class UiManager {

    private final Client client;
    private final ResizableChatPlugin plugin;
    private final ArrayList<ResizingHandles> resizingHandles = new ArrayList<>();
    private final ChatBoxBackground chatBoxBackground;
    @Getter
    @Setter
    private boolean uiCreated;
    @Getter
    @Setter
    private boolean isHandleKeybindPressed;

    @Inject
    UiManager(Client client, ResizableChatPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        resizingHandles.add(new ResizingHandles(ResizeType.VERTICAL, client, plugin));
        resizingHandles.add(new ResizingHandles(ResizeType.HORIZONTAL, client, plugin));
        chatBoxBackground = new ChatBoxBackground(client, plugin);
    }

    Widget getContainer() {
        return client.getWidget(InterfaceID.CHATBOX, 0).getParent();
    }

    public void shutDown() {
        reset();
    }

    public void reset() {
        uiCreated = false;
        plugin.resetChatbox();

        resizingHandles.forEach(b -> b.destroy(getContainer()));
        chatBoxBackground.destroy(getContainer());
    }

    public void onResizeableChanged() {
        reset();
    }


    public void create() {
        if (uiCreated || !client.isResized()) return;

        try {
            chatBoxBackground.create(getContainer());
            resizingHandles.forEach(handle -> handle.create(getContainer()));
            uiCreated = true;
        } catch (Exception e) {
            uiCreated = false;
        }
    }

    public void onChatBoxResized() {
        resizingHandles.forEach(ResizingHandles::onResize);
        chatBoxBackground.onResize();
    }

    public void onVarbitChanged() {
        resizingHandles.forEach(ResizingHandles::onVarbitChanged);

        boolean isTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
        chatBoxBackground.hideBorders(isTransparent);
    }

    public void hideResizingHandles(boolean state) {
        if (!uiCreated) return;

        resizingHandles.forEach(handle -> handle.setHidden(state));
    }

    public void updateHiddenState() {
        if (!uiCreated) return;

        resizingHandles.forEach(handle -> handle.getSlider().setHidden(handle.isHidden()));
    }

    public void setHidden(boolean hidden) {
        if (!uiCreated) return;

        hideResizingHandles(hidden);
        if (client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 0) {
            chatBoxBackground.hideBorders(hidden);
        }
    }
}
