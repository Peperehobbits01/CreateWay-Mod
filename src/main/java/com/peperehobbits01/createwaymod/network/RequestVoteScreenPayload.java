package com.peperehobbits01.createwaymod.network;

import com.peperehobbits01.createwaymod.CreateWayMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestVoteScreenPayload() implements CustomPacketPayload {
    public static final Type<RequestVoteScreenPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreateWayMod.MODID, "request_vote_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestVoteScreenPayload> STREAM_CODEC =
        StreamCodec.unit(new RequestVoteScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestVoteScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.getServer().isSingleplayer()) {
                return;
            }

            context.reply(new VoteScreenAccessPayload(VoteScreenAccessPayload.canVote(player)));
        });
    }
}
