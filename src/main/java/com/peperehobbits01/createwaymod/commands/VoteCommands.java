package com.peperehobbits01.createwaymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class VoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reopenvotes")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                VoteManager.ReopenResult result = new VoteManager((ServerLevel) source.getLevel()).reopenVotes();
                switch (result) {
                    case REOPENED -> {
                        source.sendSuccess(
                            () -> Component.translatable("command.createwaymod.vote.reopened"), true
                        );
                        return 1;
                    }
                    case NOT_CLOSED -> source.sendFailure(
                        Component.translatable("command.createwaymod.vote.not_closed")
                    );
                    case UNAVAILABLE -> source.sendFailure(
                        Component.translatable("message.createwaymod.vote.unavailable")
                    );
                }
                return 0;
            }));

        dispatcher.register(Commands.literal("setendhour")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("hour", StringArgumentType.word())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerLevel level = (ServerLevel) source.getLevel();
                    VoteManager manager = new VoteManager(level);
                    
                    String hour = StringArgumentType.getString(context, "hour");
                    if (manager.setHour(hour)) {
                        source.sendSuccess(
                            () -> Component.translatable("command.createwaymod.vote.set_hour", hour), false
                        );
                        return 1;
                    }
                    source.sendFailure(Component.translatable("command.createwaymod.vote.set_hour_failed"));
                    return 0;
                })));
    }
}
