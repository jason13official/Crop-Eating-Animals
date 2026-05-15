package io.github.jason13official.crop_eating_animals;

import net.fabricmc.api.ClientModInitializer;

public class CropEatingAnimalsClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    CropEatingAnimalsClient.init();
  }
}
