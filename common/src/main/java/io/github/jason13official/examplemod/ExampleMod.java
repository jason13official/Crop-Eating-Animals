package io.github.jason13official.examplemod;

import net.minecraft.resources.ResourceLocation;


public class ExampleMod {

  public static void init() {
  }

  public static ResourceLocation identifier(final String path) {
    return new ResourceLocation(Constants.MOD_ID, path);
  }
}