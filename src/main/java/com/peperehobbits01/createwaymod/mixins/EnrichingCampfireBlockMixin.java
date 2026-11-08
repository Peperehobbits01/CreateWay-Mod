package com.peperehobbits01.createwaymod.mixins;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.nuclearteam.createnuclear.content.enriching.campfire.EnrichingCampfireBlock")
public class EnrichingCampfireBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void addSignalFireProperty(StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder, CallbackInfo ci) {
        builder.add(BlockStateProperties.SIGNAL_FIRE);
    }
}
