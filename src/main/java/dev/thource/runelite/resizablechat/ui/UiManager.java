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
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;

import java.util.ArrayList;

@Singleton
@Slf4j
public class UiManager {

    @Getter
    @Setter
    private boolean uiCreated = false;

    private final Client client;
    private final ClientThread clientThread;

    private ResizableChatPlugin plugin;


    private ArrayList<ResizeButtons> resizeButtons = new ArrayList<>();

    private ChatBoxBackground chatBoxBackground;

    @Inject
    UiManager(Client client, ClientThread clientThread, ResizableChatPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        this.clientThread = clientThread;
        resizeButtons.add(new ResizeButtons(ResizeType.VERTICAL,client,plugin));
        resizeButtons.add(new ResizeButtons(ResizeType.HORIZONTAL,client,plugin));
        chatBoxBackground = new ChatBoxBackground(client,plugin);
    }

    Widget getContainer() {
        final Widget equipment = client.getWidget(InterfaceID.CHATBOX, 0);
        return equipment.getParent();
    }

    public void shutDown() {
        reset();
    }

    public void reset() {
        uiCreated = false;
        plugin.resetChatbox();
    }

    public void onResizeableChanged() {
        reset();
    }


    public void create() {
        if (uiCreated || !client.isResized()) return;
        try {
            chatBoxBackground.create(getContainer());
            resizeButtons.forEach( resizeButtons -> resizeButtons.create(getContainer()));
            chatBoxBackground.setupBackground();
            uiCreated = true;
        }catch (Exception e) {
            uiCreated = false;
        }
    }

    public void onChatBoxResized() {
        resizeButtons.forEach(ResizeButtons::onResize);
        chatBoxBackground.onResize();
    }

    public void onVarbitChanged() {
        resizeButtons.forEach(ResizeButtons::onVarbitChanged);

        boolean isNotTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
        System.out.println("sdfsd");
        chatBoxBackground.hideChatbox(isNotTransparent);

    }

    public void hideButtons(boolean state) {
        if (!uiCreated) return;

        resizeButtons.forEach(resizeButtons -> resizeButtons.slider.setHidden(state));

    }

}
