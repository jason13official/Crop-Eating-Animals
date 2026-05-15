package io.github.jason13official.crop_eating_animals.platform;

import io.github.jason13official.crop_eating_animals.platform.services.IPlatformHelper;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

  @Override
  public String getPlatformName() {

    return "Forge";
  }

  @Override
  public boolean isModLoaded(String modId) {

    return ModList.get().isLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {

    return !FMLLoader.isProduction();
  }

  @Override
  public Path getGameDirectory() {

    return FMLLoader.getGamePath();
  }

  @Override
  public Builder tabBuilder() {

    return CreativeModeTab.builder();
  }

  @Override
  public SpawnEggItem createSpawnEggItem(Supplier<EntityType<? extends Mob>> typeSupplier, int background, int highlight, Properties properties) {

    return new ForgeSpawnEggItem(typeSupplier, background, highlight, properties);
  }
}