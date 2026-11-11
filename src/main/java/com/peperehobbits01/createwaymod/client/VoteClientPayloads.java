package com.peperehobbits01.createwaymod.client;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.network.VoteScreenAccessPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = CreateWayMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class VoteClientPayloads {
    private static boolean votingOpen;

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
            VoteScreenAccessPayload.TYPE,
            VoteScreenAccessPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                votingOpen = payload.canVote();
                VoteClientEvents.setVotingOpen(votingOpen);
                if (payload.canVote()) {
                    VoteScreen.openAfterAccessCheck();
                } else {
                    VoteScreen.onVotingClosed();
                }
            })
        );
    }

    public static boolean isVotingOpen() {
        return votingOpen;
    }
}
