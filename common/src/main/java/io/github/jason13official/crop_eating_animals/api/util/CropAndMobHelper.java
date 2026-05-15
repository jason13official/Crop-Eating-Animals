package io.github.jason13official.crop_eating_animals.api.util;

import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CropAndMobHelper {

  // Reference defaults — replace with real config later
  public static final boolean SPREAD_CROPS  = true;
  public static final boolean INSERT_INV    = true;
  public static final boolean REMOVE_DROPS  = false;
  public static final boolean FORCE_HARVEST = false;
  public static final boolean CHEAT_SEED    = false;
  public static final int     MAX_ANIMALS   = 6;

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
    return isMatureCrop(level, cropPos) && cropState.getDrops(new Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(cropPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY)).stream().anyMatch(animal::isFood);
  }

  public static boolean isMatureCrop(Level level, BlockPos cropPos) {
    BlockState cropState = level.getBlockState(cropPos);
    return cropState.getBlock() instanceof CropBlock && ((CropBlock) cropState.getBlock()).isMaxAge(cropState);
  }

  public static boolean validLove(Animal animal) {
    return !animal.isDeadOrDying()
        && animalAllowed(animal)
        && animal.getAge() == 0
        && !animal.isInLove()
        && (MAX_ANIMALS < 0 || countNearbySiblings(animal, 5.5) <= MAX_ANIMALS);
  }

  // TODO back this with config option (blacklist/whitelist)
  public static boolean animalAllowed(Animal animal) {
    return animal.getClass() != Horse.class;
  }
}
