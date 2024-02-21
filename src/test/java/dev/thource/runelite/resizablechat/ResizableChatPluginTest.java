package dev.thource.runelite.resizablechat;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ResizableChatPluginTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(ResizableChatPlugin.class);
    RuneLite.main(args);
  }
}
