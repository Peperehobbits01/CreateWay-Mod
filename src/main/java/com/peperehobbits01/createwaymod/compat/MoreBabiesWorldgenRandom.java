package com.peperehobbits01.createwaymod.compat;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class MoreBabiesWorldgenRandom {
    private static final ThreadLocal<RandomSource> WORLDGEN_RANDOM = new ThreadLocal<>();

    private MoreBabiesWorldgenRandom() {
    }

    public static RandomSource getWorldgenRandom(Level level) {
        RandomSource worldgenRandom = WORLDGEN_RANDOM.get();
        return worldgenRandom != null ? worldgenRandom : level.getRandom();
    }

    public static RandomSource pushWorldgenRandom(RandomSource random) {
        RandomSource previous = WORLDGEN_RANDOM.get();
        WORLDGEN_RANDOM.set(random);
        return previous;
    }

    public static void restoreWorldgenRandom(RandomSource previous) {
        if (previous == null) {
            WORLDGEN_RANDOM.remove();
        } else {
            WORLDGEN_RANDOM.set(previous);
        }
    }
}
