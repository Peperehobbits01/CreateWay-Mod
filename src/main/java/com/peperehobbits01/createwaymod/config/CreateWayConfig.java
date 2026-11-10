package com.peperehobbits01.createwaymod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CreateWayConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.BooleanValue DISABLE_ADVANCEMENT_ANNOUNCEMENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_VOTE_SYSTEM;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        DISABLE_ADVANCEMENT_ANNOUNCEMENTS = builder
            .comment("Disable advancement announcements in chat by default for new worlds.")
            .define("disableAdvancementAnnouncements", true);
        ENABLE_VOTE_SYSTEM = builder
            .comment("Enable the End opening vote system.")
            .define("enableVoteSystem", true);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    private CreateWayConfig() {
    }

    public static boolean disableAdvancementAnnouncements() {
        return SERVER_SPEC.isLoaded()
            ? DISABLE_ADVANCEMENT_ANNOUNCEMENTS.get()
            : DISABLE_ADVANCEMENT_ANNOUNCEMENTS.getDefault();
    }

    public static boolean isVoteSystemEnabled() {
        return SERVER_SPEC.isLoaded()
            ? ENABLE_VOTE_SYSTEM.get()
            : ENABLE_VOTE_SYSTEM.getDefault();
    }
}
