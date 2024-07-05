package dev.thource.runelite.resizablechat;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResizeType {
    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal");

    private final String formatName;
}
