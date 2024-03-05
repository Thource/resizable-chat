package dev.thource.runelite.resizablechat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteOverride;

@RequiredArgsConstructor
public enum CustomSprites implements SpriteOverride {
    RESIZE_H(-2365, "resizeH.png"),
    RESIZE_V(-2366, "resizeV.png"),
    RESIZE_V_BROWN(-2367, "resizeVBrown.png"),
    RESIZE_H_BROWN(-2368, "resizeHBrown.png"),
    BACKGROUND(-2369, "bg.png"),
    RESIZE_H_H(-2370, "resizeHH.png"),
    RESIZE_V_H(-2371, "resizeVH.png"),
    RESIZE_V_BROWN_H(-2372, "resizeVBrownH.png"),
    RESIZE_H_BROWN_H(-2373, "resizeHBrownH.png");


    @Getter
    private final int spriteId;

    @Getter
    private final String fileName;
}