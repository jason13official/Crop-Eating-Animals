package io.github.jason13official.examplemod.platform;

import io.github.jason13official.examplemod.platform.services.IPlatformHelper;
import java.nio.file.Path;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.CreativeModeTab.Builder;

public class FabricPlatformHelper implements IPlatformHelper {

  @Override
  public String getPlatformName() {

    return "Fabric";
  }

  @Override
  public boolean isModLoaded(String modId) {

    return FabricLoader.getInstance().isModLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {

    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }

  @Override
  public Path getGameDirectory() {

    return FabricLoader.getInstance().getGameDir();
  }

  @Override
  public Builder tabBuilder() {

    return FabricCreativeModeTab.builder();
  }
}
