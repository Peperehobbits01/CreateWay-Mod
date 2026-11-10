package com.peperehobbits01.createwaymod;

import com.peperehobbits01.createwaymod.commands.VoteCommands;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.network.CastVotePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
            CastVotePayload.TYPE,
            CastVotePayload.STREAM_CODEC,
            CastVotePayload::handle
        );
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        VoteCommands.register(event.getDispatcher());
    }
}
