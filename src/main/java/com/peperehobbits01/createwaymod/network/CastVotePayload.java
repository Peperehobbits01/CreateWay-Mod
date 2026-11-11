package com.peperehobbits01.createwaymod.network;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record CastVotePayload(List<String> dates) implements CustomPacketPayload {
    public static final int MAX_DATES = VoteManager.MAX_DATES_PER_BALLOT;
    public static final Type<CastVotePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreateWayMod.MODID, "cast_vote"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastVotePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(MAX_DATES)),
            CastVotePayload::dates,
            CastVotePayload::new
        );

    public CastVotePayload {
        dates = List.copyOf(dates);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CastVotePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.getServer().isSingleplayer()) {
                return;
            }

            VoteManager.VoteBatchResult result = new VoteManager(player.serverLevel())
                .castVotes(player.getUUID(), payload.dates());
            VoteScreenAccessPayload.sendToAll(player.getServer());
            Component message = switch (result.status()) {
                case ACCEPTED -> Component.translatable(
                    "message.createwaymod.vote.accepted", result.accepted(), result.alreadyVoted()
                );
                case CLOSED -> Component.translatable("message.createwaymod.vote.closed");
                case INVALID_DATES -> Component.translatable("message.createwaymod.vote.invalid_dates", MAX_DATES);
                case UNAVAILABLE -> Component.translatable("message.createwaymod.vote.unavailable");
            };
            player.sendSystemMessage(message);
        });
    }
}
