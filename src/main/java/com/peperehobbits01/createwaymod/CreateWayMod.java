package com.peperehobbits01.createwaymod;

import com.peperehobbits01.createwaymod.commands.VoteCommands;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.network.CastVotePayload;
import com.peperehobbits01.createwaymod.network.RequestVoteScreenPayload;
import com.peperehobbits01.createwaymod.network.VoteScreenAccessPayload;
import com.peperehobbits01.createwaymod.util.TimeUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateWayMod.MODID)
public class CreateWayMod {
    public static final String MODID = "createwaymod";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public CreateWayMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing CreateWay Mod");
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, CreateWayConfig.SERVER_SPEC);
        modEventBus.addListener(this::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
            CastVotePayload.TYPE,
            CastVotePayload.STREAM_CODEC,
            CastVotePayload::handle
        );
        registrar.playToServer(
            RequestVoteScreenPayload.TYPE,
            RequestVoteScreenPayload.STREAM_CODEC,
            RequestVoteScreenPayload::handle
        );
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            registrar.playToClient(
                VoteScreenAccessPayload.TYPE,
                VoteScreenAccessPayload.STREAM_CODEC,
                (payload, context) -> {
                }
            );
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        VoteCommands.register(event.getDispatcher());
    }

    private void onServerStopped(ServerStoppedEvent event) {
        TimeUtils.setDebugEndMode(TimeUtils.DebugEndMode.NORMAL);
    }
}
