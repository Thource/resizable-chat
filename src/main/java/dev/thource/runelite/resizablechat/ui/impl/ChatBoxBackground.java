package dev.thource.runelite.resizablechat.ui.impl;

import com.google.inject.Singleton;
import dev.thource.runelite.resizablechat.CustomSprites;
import dev.thource.runelite.resizablechat.ResizableChatConfig;
import dev.thource.runelite.resizablechat.ResizableChatPlugin;
import dev.thource.runelite.resizablechat.ui.UI;
import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

/**
 * UI class for ChatBoxBackground.
 */
@Singleton
public class ChatBoxBackground extends UI {

    static final int BORDER_SIZE = 32;

    private final Client client;
    private final ResizableChatConfig config;

    private Widget topLeftBrace;
    private Widget topRightBrace;
    private Widget bottomLeftBrace;
    private Widget bottomRightBrace;

    private Widget leftSide;
    private Widget topSide;
    private Widget rightSide;
    private Widget bottomSide;


    /**
     * Constructor for ChatBoxBackground.
     *
     * @param client The RuneLite client.
     * @param plugin The ResizableChatPlugin instance.
     */
    public ChatBoxBackground(Client client, ResizableChatPlugin plugin) {
        this.client = client;
        this.config = plugin.getConfig();
    }

    /**
     * Create resize buttons.
     *
     * @param parent The parent widget.
     */
    @Override
    public void create(Widget parent) {
        setupBackground();

        topLeftBrace = createSpriteWidget(BORDER_SIZE, BORDER_SIZE);
        topRightBrace = createSpriteWidget(BORDER_SIZE, BORDER_SIZE);
        bottomLeftBrace = createSpriteWidget(BORDER_SIZE, BORDER_SIZE);
        bottomRightBrace = createSpriteWidget(BORDER_SIZE, BORDER_SIZE);

        leftSide = createSpriteWidget(BORDER_SIZE, config.chatHeight() - BORDER_SIZE * 2);
        topSide = createSpriteWidget(config.chatWidth() - BORDER_SIZE * 2, BORDER_SIZE);
        rightSide = createSpriteWidget(BORDER_SIZE, config.chatHeight() - BORDER_SIZE * 2);
        bottomSide = createSpriteWidget(config.chatWidth() - BORDER_SIZE * 2, BORDER_SIZE);

        topLeftBrace.setSpriteId(958);
        topRightBrace.setSpriteId(959);
        bottomLeftBrace.setSpriteId(960);
        bottomRightBrace.setSpriteId(961);
        leftSide.setSpriteId(955);
        topSide.setSpriteId(954);
        rightSide.setSpriteId(957);
        bottomSide.setSpriteId(956);

        updateBox(config.chatWidth(), config.chatHeight());

        boolean isTransparent = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
        if (isTransparent) {
            hideBorders(true);
        }
    }

    @Override
    public void destroy(Widget parent) {
        Widget chatbox = client.getWidget(ComponentID.CHATBOX_FRAME);
        Widget[] children = chatbox.getChildren();

        children[topLeftBrace.getIndex()] = null;
        children[topRightBrace.getIndex()] = null;
        children[bottomLeftBrace.getIndex()] = null;
        children[bottomRightBrace.getIndex()] = null;
        children[leftSide.getIndex()] = null;
        children[topSide.getIndex()] = null;
        children[rightSide.getIndex()] = null;
        children[bottomSide.getIndex()] = null;
    }

    private void updateBox(int width, int height) {
        int x = 0;
        int y = 4;

        setupBackground();

        setSize(topLeftBrace, BORDER_SIZE, BORDER_SIZE);
        setSize(topRightBrace, BORDER_SIZE, BORDER_SIZE);
        setSize(bottomLeftBrace, BORDER_SIZE, BORDER_SIZE);
        setSize(bottomRightBrace, BORDER_SIZE, BORDER_SIZE);

        setSize(leftSide, BORDER_SIZE, config.chatHeight() - BORDER_SIZE * 2);
        setSize(topSide, config.chatWidth() - BORDER_SIZE * 2, BORDER_SIZE);
        setSize(rightSide, BORDER_SIZE, config.chatHeight() - BORDER_SIZE * 2);
        setSize(bottomSide, config.chatWidth() - BORDER_SIZE * 2, BORDER_SIZE);


        layoutWidget(topLeftBrace, x, y);
        layoutWidget(topRightBrace, x + width - BORDER_SIZE, y);
        layoutWidget(bottomLeftBrace, x, y + height - BORDER_SIZE);
        layoutWidget(bottomRightBrace, x + width - BORDER_SIZE, y + height - BORDER_SIZE);

        layoutWidget(leftSide, x, y + BORDER_SIZE);
        layoutWidget(topSide, x + BORDER_SIZE, y);
        layoutWidget(rightSide, x + width - BORDER_SIZE, y + BORDER_SIZE);
        layoutWidget(bottomSide, x + BORDER_SIZE, y + height - BORDER_SIZE);

    }

    public void setupBackground() {
        Widget w = client.getWidget(ComponentID.CHATBOX_TRANSPARENT_BACKGROUND);

        Widget child = w.getChild(0);
        // These are deprecated but using originalY and originalWidth/Height doesn't work for this one?
        child.setRelativeY(4);
        child.setSpriteId(CustomSprites.BACKGROUND.getSpriteId());
        child.setWidth(config.chatWidth());
        child.setHeight(config.chatHeight());
        child.setSpriteTiling(true);
    }

    public void hideBorders(boolean state) {
        if (topLeftBrace == null) {
            return;
        }

        topLeftBrace.setHidden(state);
        topRightBrace.setHidden(state);
        bottomLeftBrace.setHidden(state);
        bottomRightBrace.setHidden(state);
        leftSide.setHidden(state);
        topSide.setHidden(state);
        rightSide.setHidden(state);
        bottomSide.setHidden(state);
    }


    protected void layoutWidget(Widget w, int x, int y) {
        w.setOriginalX(x);
        w.setOriginalY(y);
        w.revalidate();
    }

    protected void setSize(Widget w, int width, int height) {
        w.setOriginalWidth(width);
        w.setOriginalHeight(height);
    }

    protected Widget createSpriteWidget(int width, int height) {
        Widget chatbox = client.getWidget(ComponentID.CHATBOX_FRAME);

        final Widget w = chatbox.createChild(-1, WidgetType.GRAPHIC);
        w.setOriginalWidth(width);
        w.setOriginalHeight(height);
        w.setSpriteTiling(true);
        return w;
    }

    @Override
    protected void onButtonClicked(ScriptEvent scriptEvent) {
        // do nothing
    }

    @Override
    protected void onVarbitChanged() {
        // do nothing
    }

    public void onResize() {
        updateBox(config.chatWidth(), config.chatHeight());
    }
}
