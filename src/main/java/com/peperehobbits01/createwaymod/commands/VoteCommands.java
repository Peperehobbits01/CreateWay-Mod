package com.peperehobbits01.createwaymod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import com.peperehobbits01.createwaymod.network.VoteScreenAccessPayload;
import com.peperehobbits01.createwaymod.util.TimeUtils;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class VoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("createway")
            .then(Commands.literal("debug")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("end")
                    .then(Commands.literal("open")
                        .executes(context -> setDebugEndMode(
                            context.getSource(), TimeUtils.DebugEndMode.OPEN,
                            "command.createwaymod.debug.end.open"
                        )))
                    .then(Commands.literal("closed")
                        .executes(context -> setDebugEndMode(
                            context.getSource(), TimeUtils.DebugEndMode.CLOSED,
                            "command.createwaymod.debug.end.closed"
                        )))
                    .then(Commands.literal("normal")
                        .executes(context -> setDebugEndMode(
                            context.getSource(), TimeUtils.DebugEndMode.NORMAL,
                            "command.createwaymod.debug.end.normal"
                        ))))
                .then(Commands.literal("vote")
                    .then(Commands.literal("complete")
                        .executes(context -> completeVoteForDebug(context.getSource()))))));

        dispatcher.register(Commands.literal("reopenvotes")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                VoteManager.ReopenResult result = new VoteManager((ServerLevel) source.getLevel()).reopenVotes();
                switch (result) {
                    case REOPENED -> {
                        VoteScreenAccessPayload.sendToAll(source.getServer());
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
            .then(Commands.argument("hour", StringArgumentType.greedyString())
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

    private static int setDebugEndMode(
        CommandSourceStack source,
        TimeUtils.DebugEndMode mode,
        String translationKey
    ) {
        TimeUtils.setDebugEndMode(mode);
        source.sendSuccess(() -> Component.translatable(translationKey), true);
        return 1;
    }

    private static int completeVoteForDebug(CommandSourceStack source) {
        VoteManager manager = new VoteManager((ServerLevel) source.getLevel());
        if (!manager.completeVoteForDebug()) {
            source.sendFailure(Component.translatable("command.createwaymod.debug.vote.failed"));
            return 0;
        }

        TimeUtils.setDebugEndMode(TimeUtils.DebugEndMode.NORMAL);
        VoteScreenAccessPayload.sendToAll(source.getServer());
        source.sendSuccess(
            () -> Component.translatable("command.createwaymod.debug.vote.completed"),
            true
        );
        return 1;
    }
}
