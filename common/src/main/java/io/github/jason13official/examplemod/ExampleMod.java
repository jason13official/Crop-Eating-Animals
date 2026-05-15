package io.github.jason13official.examplemod;

import net.minecraft.resources.Identifier;

public class ExampleMod {

  public static void init() {
  }

  public static Identifier identifier(final String path) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
  }
}