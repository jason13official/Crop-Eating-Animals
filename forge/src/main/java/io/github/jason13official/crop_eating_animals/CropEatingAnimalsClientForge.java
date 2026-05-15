package io.github.jason13official.crop_eating_animals;

import java.util.function.Consumer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CropEatingAnimalsClientForge {

  public CropEatingAnimalsClientForge(final IEventBus modEventBus) {

    modEventBus.addListener((Consumer<FMLClientSetupEvent>) event -> CropEatingAnimalsClient.init());
  }
}
