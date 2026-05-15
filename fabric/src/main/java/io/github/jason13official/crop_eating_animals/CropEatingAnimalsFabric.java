package io.github.jason13official.crop_eating_animals;

import io.github.jason13official.crop_eating_animals.impl.common.registry.ModBlocks;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModEntities;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModItems;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModMenus;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModParticles;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModTabs;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class CropEatingAnimalsFabric implements ModInitializer {

  @Override
  public void onInitialize() {

    bind(BuiltInRegistries.BLOCK, ModBlocks::register);
    bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
    bind(BuiltInRegistries.ITEM, ModItems::register);
    bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);
    bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(BuiltInRegistries.MENU, ModMenus::register);
    bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModTabs::register);

    CropEatingAnimals.init();

    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ResourceReloadListener());
  }

  public <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {

    source.accept((t, rl) -> Registry.register(registry, rl, t));
  }

  public static class ResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
      return CropEatingAnimals.identifier(Constants.MOD_ID);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
      // ModConfig.load(Services.PLATFORM.getConfigDirectory());
    }
  }
}
