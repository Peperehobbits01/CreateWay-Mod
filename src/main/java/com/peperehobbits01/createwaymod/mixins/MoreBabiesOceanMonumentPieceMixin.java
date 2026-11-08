package com.peperehobbits01.createwaymod.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.peperehobbits01.createwaymod.compat.MoreBabiesWorldgenRandom;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(
    targets = "net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces$OceanMonumentPiece",
    priority = 1100
)
public abstract class MoreBabiesOceanMonumentPieceMixin {
    @WrapMethod(method = "spawnElder")
    private void createway$useWorldgenRandom(
        WorldGenLevel level,
        BoundingBox boundingBox,
        int x,
        int y,
        int z,
        Operation<Void> original
    ) {
        RandomSource previous = MoreBabiesWorldgenRandom.pushWorldgenRandom(level.getRandom());
        try {
            original.call(level, boundingBox, x, y, z);
        } finally {
            MoreBabiesWorldgenRandom.restoreWorldgenRandom(previous);
        }
    }
}
