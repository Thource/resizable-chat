package dev.thource.runelite.resizablechat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResizingHandleMode {
  ALWAYS("Always"),
  DRAG("Drag mode"),
  NEVER("Never");

  private final String name;

  @Override
  public String toString() {
    return name;
  }
}
