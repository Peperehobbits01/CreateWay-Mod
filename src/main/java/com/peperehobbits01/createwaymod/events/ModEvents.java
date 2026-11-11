package com.peperehobbits01.createwaymod.events;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import com.peperehobbits01.createwaymod.data.VoteData;
import com.peperehobbits01.createwaymod.util.TimeUtils;
import com.peperehobbits01.createwaymod.network.VoteScreenAccessPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.time.LocalDateTime;

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
            VoteScreenAccessPayload.sendTo(player);

            VoteData data = manager.getData();
            if (data != null && data.isVoteFinished && data.selectedDate != null) {
                if (data.selectedHour == null) {
                    if (player.getServer().getPlayerList().isOp(player.getGameProfile())) {
                        showTitle(
                            player,
                            "title.createwaymod.vote.finished",
                            "title.createwaymod.vote.set_hour",
                            data.selectedDate
                        );
                    }
                } else {
                    showTitle(
                        player,
                        "title.createwaymod.end.opening",
                        "title.createwaymod.end.opening_details",
                        data.selectedDate,
                        data.selectedHour
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!CreateWayConfig.isVoteSystemEnabled()) {
            return;
        }

        var server = event.getServer();
        if (server.isSingleplayer()) {
            return;
        }

        VoteData data = new VoteManager(server.overworld()).getData();
        LocalDateTime openingTime = TimeUtils.getScheduledOpeningTime(data);
        if (data == null || data.openingAnnouncementSent || openingTime == null
            || LocalDateTime.now().isBefore(openingTime)) {
            return;
        }

        data.openingAnnouncementSent = true;
        data.setDirty();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            showTitle(
                player,
                "title.createwaymod.end.opening_now",
                "title.createwaymod.end.opening_details",
                data.selectedDate,
                data.selectedHour
            );
        }
    }

    private static void showTitle(ServerPlayer player, String titleKey, String subtitleKey, Object... subtitleArgs) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(titleKey)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
            Component.translatable(subtitleKey, subtitleArgs)
        ));
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0F, 1.0F);
    }
}
