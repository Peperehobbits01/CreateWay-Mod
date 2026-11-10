package com.peperehobbits01.createwaymod.events;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import com.peperehobbits01.createwaymod.data.VoteData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = CreateWayMod.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getServer().isSingleplayer() || !CreateWayConfig.isVoteSystemEnabled()) {
                return;
            }

            VoteManager manager = new VoteManager(player.serverLevel());
            manager.updateLogin(player.getUUID());

            VoteData data = manager.getData();
            if (data != null && data.isVoteFinished && data.selectedDate != null && data.selectedHour != null) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("title.createwaymod.end.opening")
                ));
                player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("title.createwaymod.end.opening_details", data.selectedDate, data.selectedHour)
                ));
            }
        }
    }
}
