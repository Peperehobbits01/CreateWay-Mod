package com.peperehobbits01.createwaymod.network;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public record VoteScreenAccessPayload(boolean canVote) implements CustomPacketPayload {
    public static final Type<VoteScreenAccessPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreateWayMod.MODID, "vote_screen_access"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VoteScreenAccessPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL,
            VoteScreenAccessPayload::canVote,
            VoteScreenAccessPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new VoteScreenAccessPayload(canVote(player)));
    }

    public static void sendToAll(MinecraftServer server) {
        if (server.isSingleplayer()) {
            return;
        }
        var data = new VoteManager(server.overworld()).getData();
        boolean canVote = CreateWayConfig.isVoteSystemEnabled() && data != null && !data.isVoteFinished;
        PacketDistributor.sendToAllPlayers(new VoteScreenAccessPayload(canVote));
    }

    public static boolean canVote(ServerPlayer player) {
        var data = new VoteManager(player.serverLevel()).getData();
        return !player.getServer().isSingleplayer()
            && CreateWayConfig.isVoteSystemEnabled()
            && data != null
            && !data.isVoteFinished;
    }
}
