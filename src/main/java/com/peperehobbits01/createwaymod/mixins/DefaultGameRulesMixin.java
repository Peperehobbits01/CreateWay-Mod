package com.peperehobbits01.createwaymod.mixins;

import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public abstract class DefaultGameRulesMixin {
    @Shadow
    public abstract <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> key);

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void createway$disableAdvancementAnnouncementsByDefault(CallbackInfo callbackInfo) {
        if (CreateWayConfig.disableAdvancementAnnouncements()) {
            getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, null);
        }
    }
}
