package io.github.jason13official.crop_eating_animals.api.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Mob2CropNavigation {

  public static void moveToNextCrop(Animal animal) {
    Level level = animal.level();

    BlockPos.MutableBlockPos entPos = animal.blockPosition().mutable();
    BlockPos.MutableBlockPos mut1 = entPos.mutable();
    BlockPos.MutableBlockPos mut2 = entPos.mutable();
    List<BlockPos> posList = Lists.newLinkedList(BlockPos.betweenClosed(mut1.move(-7, -2, -7), mut2.move(7, 2, 7))).stream()
        .filter(blockPos -> CropAndMobHelper.isValidCrop(animal, blockPos) && CropAndMobHelper.validLove(animal)).toList();

    posList.sort((pos1, pos2) -> Double.compare(pos1.distSqr(entPos), pos2.distSqr(entPos)));

    boolean walk = false;
    for (BlockPos p : posList) {
      if (animal.getNavigation().createPath(p, 0) == null || !CropAndMobHelper.isNearPartner(animal, p)) {
        continue;
      }
      animal.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.2);
      walk = true;
    }

    // TODO fix and add config option
//    if (CropEatingAnimals.forceHarvest && !walk && !ani.isInLove()) {
    if (!walk && !animal.isInLove()) {

      List<BlockPos> posList2 = Lists.newLinkedList(BlockPos.betweenClosed(mut1.move(-7, -2, -7), mut2.move(7, 2, 7)))
          .stream()
          .filter(p -> CropAndMobHelper.isMatureCrop(animal.level(), p))
          .collect(Collectors.toList());

      posList2.sort((pos1, pos2) -> Double.compare(pos1.distSqr(entPos), pos2.distSqr(entPos)));

      for (BlockPos p : posList2) {
        if (animal.getNavigation().createPath(p, 0) == null) {
          continue;
        }
        animal.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.2);
      }
    }
  }
}
