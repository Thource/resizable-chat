package dev.thource.runelite.resizablechat.ui;

import net.runelite.api.ScriptEvent;
import net.runelite.api.widgets.Widget;

public abstract class UI {
    protected abstract void create(Widget parent);

    protected abstract void destroy(Widget parent);

    protected abstract void onButtonClicked(ScriptEvent scriptEvent);

    protected abstract void onVarbitChanged();

}
