package com.peperehobbits01.createwaymod.mixins;

import com.peperehobbits01.createwaymod.util.TimeUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void blockEyeUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (!TimeUtils.isEndOpen(serverLevel)) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void blockEyePlacement(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
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
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
