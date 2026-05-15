package io.github.jason13official.crop_eating_animals;

import com.google.common.collect.Lists;
import io.github.jason13official.crop_eating_animals.api.util.CropAndMobHelper;
import io.github.jason13official.crop_eating_animals.api.util.Mob2CropNavigation;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModBlocks;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModEntities;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModItems;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModMenus;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModParticles;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModTabs;
import io.github.jason13official.crop_eating_animals.impl.common.registry.ModTiles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
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

    // Eat event — fires every 15 ticks per animal
    MinecraftForge.EVENT_BUS.addListener((Consumer<LivingTickEvent>) event -> {

      LivingEntity living = event.getEntity();
      Level world = living.level();

      if (!(living instanceof Animal animal) || !(world instanceof ServerLevel level) || living.tickCount % 15 != 0) {
        return;
      }

      BlockPos current = animal.blockPosition();

      if (animal.getY() - Mth.floor(animal.getY()) > 0.5) {
        current = current.above();
      }

      if (CropAndMobHelper.isValidCrop(animal, current) && CropAndMobHelper.validLove(animal) && CropAndMobHelper.isNearPartner(animal, current)) {

        List<ItemStack> drops = breakAndReplant(level, current);
        ItemStack food = null;
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
          ItemStack s = it.next();
          if (animal.isFood(s)) {
            food = s;
            it.remove();
            break;
          }
        }
        if (food != null) {
          animal.setInLove(null);
        } else {
          Mob2CropNavigation.moveToNextCrop(animal);
        }
        handleRemainingDrops(drops, level, current);

      } else if (CropAndMobHelper.FORCE_HARVEST && CropAndMobHelper.isMatureCrop(world, current) && !animal.isInLove()) {

        List<ItemStack> drops = breakAndReplant(level, current);
        handleRemainingDrops(drops, level, current);
        if (animal.getRandom().nextBoolean()) {
          Direction[] dirs = Direction.values();
          Direction dir = dirs[animal.getRandom().nextInt(dirs.length)];
          BlockPos p = current.relative(dir, animal.getRandom().nextInt(2) + 1);
          animal.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.2);
        }
      }
    });

    // Walk event — fires every 60 ticks, guides animal toward nearest valid crop
    MinecraftForge.EVENT_BUS.addListener((Consumer<LivingTickEvent>) event -> {
      LivingEntity living = event.getEntity();
      if (!(living instanceof Animal animal) || !(animal.level() instanceof ServerLevel) || living.tickCount % 60 != 0) {
        return;
      }
      Mob2CropNavigation.moveToNextCrop(animal);
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

  private static List<ItemStack> breakAndReplant(ServerLevel level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    CropBlock crop = (CropBlock) state.getBlock();
    List<ItemStack> drops = Lists.newLinkedList(crop.getDrops(state, new Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY)));
    drops.removeAll(Collections.singleton(null));

    BlockState neww = CropAndMobHelper.CHEAT_SEED ? state.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();

    Iterator<ItemStack> it = drops.iterator();
    boolean changed = false;
    while (it.hasNext()) {
      ItemStack s = it.next();
      if (s.getItem() instanceof IPlantable plant) {
        BlockState plantState = plant.getPlant(level, pos);
        if (plantState != null && plantState.getBlock() == crop) {
          neww = plantState;
          it.remove();
          changed = true;
          break;
        }
      }
    }

    if (CropAndMobHelper.CHEAT_SEED && !changed && !drops.isEmpty()) {
      drops.remove(0);
    }

    level.setBlockAndUpdate(pos, neww);
    return drops;
  }

  private static void handleRemainingDrops(List<ItemStack> drops, ServerLevel level, BlockPos pos) {
    if (CropAndMobHelper.REMOVE_DROPS) return;

    if (CropAndMobHelper.SPREAD_CROPS) {
      List<Direction> faces = new ArrayList<>(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
      Collections.shuffle(faces);
      for (Direction face : faces) {
        BlockPos neighbor = pos.relative(face);
        if (!level.isEmptyBlock(neighbor)) continue;
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
          ItemStack k = it.next();
          if (k.getItem() instanceof IPlantable plant) {
            BlockState soil = level.getBlockState(neighbor.below());
            if (soil.getBlock().canSustainPlant(soil, level, neighbor.below(), Direction.UP, plant)) {
              level.setBlockAndUpdate(neighbor, plant.getPlant(level, pos));
              it.remove();
            }
          }
        }
      }
    }

    if (CropAndMobHelper.INSERT_INV) {
      LinkedList<BlockPos> queue = new LinkedList<>(Collections.singleton(pos));
      Set<BlockPos> visited = new HashSet<>();
      Map<IItemHandler, BlockPos> handlers = new LinkedHashMap<>();
      while (!queue.isEmpty()) {
        BlockPos cur = queue.poll();
        for (Direction face : Direction.Plane.HORIZONTAL) {
          BlockPos next = cur.relative(face);
          if (!level.isLoaded(next)) continue;
          BlockState bs = level.getBlockState(next);
          if (bs.getBlock() instanceof CropBlock
              || level.getBlockState(next.below()).getBlock() == Blocks.FARMLAND
              || level.getBlockState(next.below()).getBlock().isFertile(level.getBlockState(next.below()), level, next.below())) {
            if (visited.add(next)) queue.add(next);
          } else {
            BlockEntity be = level.getBlockEntity(next);
            if (be != null) {
              be.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite())
                .ifPresent(h -> handlers.put(h, next));
            }
          }
        }
      }
      for (IItemHandler handler : handlers.keySet()) {
        for (int i = 0; i < drops.size(); i++) {
          ItemStack s = drops.get(i);
          if (s == null || s.isEmpty()) continue;
          drops.set(i, ItemHandlerHelper.insertItemStacked(handler, s, false));
        }
      }
      drops.removeIf(s -> s == null || s.isEmpty());
    }

    for (ItemStack s : drops) {
      Block.popResource(level, pos, s);
    }
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
