package com.peperehobbits01.createwaymod.events;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import com.peperehobbits01.createwaymod.data.VoteData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = CreateWayMod.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getServer().isSingleplayer()) {
                return;
            }

            VoteManager manager = new VoteManager(player.serverLevel());
            manager.updateLogin(player.getUUID());

            VoteData data = manager.getData();
            if (data != null && data.isVoteFinished && player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable(
                    "message.createwaymod.vote.finished", data.selectedDate
                ));
            }
        }
    }
}
