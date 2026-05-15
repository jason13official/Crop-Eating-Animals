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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class CropEatingAnimalsForge {

  public static IEventBus EVENT_BUS;

  public CropEatingAnimalsForge(final FMLJavaModLoadingContext context) {
    EVENT_BUS = context.getModEventBus();

    bind(Registries.BLOCK, ModBlocks::register);
    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);
    bind(Registries.PARTICLE_TYPE, ModParticles::register);
    bind(Registries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(Registries.MENU, ModMenus::register);
    bind(Registries.CREATIVE_MODE_TAB, ModTabs::register);

    EVENT_BUS.addListener((Consumer<FMLCommonSetupEvent>) event -> CropEatingAnimals.init());

    MinecraftForge.EVENT_BUS.addListener((Consumer<AddReloadListenerEvent>) event -> {
      event.addListener(new ResourceReloadListener());
    });

    if (FMLLoader.getDist() == Dist.CLIENT) {
      new CropEatingAnimalsClientForge(EVENT_BUS);
    }
  }

  @Deprecated
  @SuppressWarnings("all")
  public CropEatingAnimalsForge() {
    this(FMLJavaModLoadingContext.get());
  }

  public <T> void bind(ResourceKey<Registry<T>> registryKey, Consumer<BiConsumer<T, ResourceLocation>> source) {

    EVENT_BUS.addListener((Consumer<RegisterEvent>) event -> {
      if (registryKey.equals(event.getRegistryKey())) {
        source.accept((t, rl) -> event.register(registryKey, rl, () -> t));
      }
    });
  }

  public static class ResourceReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    public String getName() {
      return CropEatingAnimals.identifier(Constants.MOD_ID).toString();
    }

    @Override
    protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      // ModConfig.load(Services.PLATFORM.getConfigDirectory());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      return null;
    }
  }
}