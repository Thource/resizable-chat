package dev.thource.runelite.resizablechat.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.thource.runelite.resizablechat.ResizableChatPlugin;
import dev.thource.runelite.resizablechat.ResizeType;
import dev.thource.runelite.resizablechat.ui.impl.ChatBoxBackground;
import dev.thource.runelite.resizablechat.ui.impl.ResizeButtons;
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
    private final ArrayList<ResizeButtons> resizeButtons = new ArrayList<>();
    private final ChatBoxBackground chatBoxBackground;
    @Getter
    @Setter
    private boolean uiCreated = false;

    @Inject
    UiManager(Client client, ResizableChatPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        resizeButtons.add(new ResizeButtons(ResizeType.VERTICAL, client, plugin));
        resizeButtons.add(new ResizeButtons(ResizeType.HORIZONTAL, client, plugin));
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

        resizeButtons.forEach(b -> b.destroy(getContainer()));
        chatBoxBackground.destroy(getContainer());
    }

    public void onResizeableChanged() {
        reset();
    }


    public void create() {
        if (uiCreated || !client.isResized()) return;

        try {
            chatBoxBackground.create(getContainer());
            resizeButtons.forEach(button -> button.create(getContainer()));
            uiCreated = true;
        } catch (Exception e) {
            uiCreated = false;
        }
    }

    public void onChatBoxResized() {
        resizeButtons.forEach(ResizeButtons::onResize);
        chatBoxBackground.onResize();
    }

    public void onVarbitChanged() {
        resizeButtons.forEach(ResizeButtons::onVarbitChanged);

        boolean isTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
        chatBoxBackground.hideBorders(isTransparent);
    }

    public void hideButtons(boolean state) {
        if (!uiCreated) return;

        resizeButtons.forEach(button -> button.getSlider().setHidden(state));
    }
}
