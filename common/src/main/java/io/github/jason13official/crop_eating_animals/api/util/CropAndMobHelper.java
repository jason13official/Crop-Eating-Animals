package io.github.jason13official.crop_eating_animals.api.util;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;

public class CropAndMobHelper {

  private static final BiPredicate<Animal, Animal> IS_SIBLING = CropAndMobHelper::isSibling;

  private static boolean isSibling(Animal animal, Animal tested) {
    return tested.getClass() == animal.getClass() && tested != animal && !tested.isBaby();
  }

  private static final BiPredicate<Animal, Animal> IS_PARTNER = CropAndMobHelper::isPartner;

  private static boolean isPartner(Animal animal, Animal tested) {
    return !tested.isDeadOrDying() && tested.getClass() == animal.getClass() && tested != animal && animal.getNavigation().createPath(tested, 0) != null && (tested.getAge() == 0 || tested.isInLove());
  }

  public static int countNearbySiblings(Animal animal, double range) {

    Level level = animal.level();

    return level.getEntitiesOfClass(Animal.class, new AABB(animal.blockPosition()).inflate(range))
        .stream()
        .filter(tested -> IS_SIBLING.test(animal, tested))
        .toList()
        .size();
  }

  public static boolean isNearPartner(Animal animal, BlockPos cropPos) {

    Level level = animal.level();

    return level.getEntitiesOfClass(Animal.class, new AABB(cropPos).inflate(8.0D))
        .stream()
        .anyMatch(tested -> IS_PARTNER.test(animal, tested));
  }

  public static boolean isValidCrop(Animal animal, BlockPos cropPos) {

    Level level = animal.level();

    if (!(level instanceof ServerLevel serverLevel)) {
      return false;
    }

    BlockState cropState = level.getBlockState(cropPos);

    return isMatureCrop(level, cropPos) && cropState.getDrops(new Builder(serverLevel)).stream().anyMatch(animal::isFood);
  }

  public static boolean isMatureCrop(Level level, BlockPos cropPos) {

    BlockState cropState = level.getBlockState(cropPos);

    return cropState.getBlock() instanceof CropBlock && ((CropBlock) cropState.getBlock()).isMaxAge(cropState);
  }

  // TODO config option for max animal limit
  public static boolean validLove(Animal animal) {
    return !animal.isDeadOrDying() && animalAllowed(animal) && animal.getAge() == 0 && !animal.isInLove();
  }

  // TODO back this with config option
  public static boolean animalAllowed(Animal animal) {
    return animal.getClass() != Horse.class;
  }

  private static List<ItemStack> breakAndReplant(Level world, BlockPos pos) {

    if (!(world instanceof ServerLevel serverLevel)) {
      return List.of();
    }

    BlockState state = world.getBlockState(pos);
    CropBlock crop = (CropBlock) state.getBlock();
    List<ItemStack> drops = Lists.newLinkedList(crop.getDrops(state, new Builder(serverLevel)));
    drops.removeAll(Collections.singleton(null));

    // TODO fix and add config
    // IBlockState neww = CropEatingAnimals.cheatSeed ? state.getBlock().getDefaultState() : Blocks.AIR.getDefaultState();
    BlockState neww = state.getBlock().defaultBlockState();

    Iterator<ItemStack> it = drops.iterator();
    boolean changed = false;
    while (it.hasNext()) {
      ItemStack s = it.next();

      // TODO check if we can re-plant a seed
//      if (s.getItem() instanceof IPlantable) {
//        IPlantable plant = (IPlantable) s.getItem();
//        if (/*plant.getPlantType(world, pos) == EnumPlantType.Crop && */plant.getPlant(world, pos) != null && plant.getPlant(world, pos).getBlock() == crop) {
//          neww = plant.getPlant(world, pos);
//          it.remove();
//          changed = true;
//          break;
//        }
//      }
    }

    // TODO add config option
//    if (CropEatingAnimals.cheatSeed && !changed && !drops.isEmpty()) {
    if (!changed && !drops.isEmpty()) {
      drops.remove(0);
    }

    // world.setBlockState(pos, neww);
    world.setBlockAndUpdate(pos, neww);
    return drops;
  }
}
