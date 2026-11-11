package com.peperehobbits01.createwaymod.mixins;

import com.peperehobbits01.createwaymod.util.TimeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.teamremastered.endrem.item.EREnderEye")
public abstract class EndRemasteredEyeMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void createway$blockEyePlacementBeforeEndOpens(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> callbackInfo
    ) {
        if (context.getLevel() instanceof ServerLevel serverLevel
            && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.END_PORTAL_FRAME)
            && !context.getLevel().getBlockState(context.getClickedPos()).getValue(EndPortalFrameBlock.HAS_EYE)
            && !TimeUtils.isEndOpen(serverLevel)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.translatable("message.createwaymod.end.vote_not_done"),
                    true
                );
            }
            callbackInfo.setReturnValue(InteractionResult.FAIL);
        }
    }
}
