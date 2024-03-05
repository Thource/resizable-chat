package dev.thource.runelite.resizablechat;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResizeType {
    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal");

    private final String formatName;

    public String getFormatName() {
        return formatName;
    }



}
