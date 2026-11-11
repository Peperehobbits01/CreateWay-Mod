package com.peperehobbits01.createwaymod.util;

import com.peperehobbits01.createwaymod.data.VoteData;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import net.minecraft.server.level.ServerLevel;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class TimeUtils {
    private static final DateTimeFormatter HOUR_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static volatile DebugEndMode debugEndMode = DebugEndMode.NORMAL;

    public static boolean isEndOpen(ServerLevel level) {
        if (debugEndMode == DebugEndMode.OPEN) {
            return true;
        }
        if (debugEndMode == DebugEndMode.CLOSED) {
            return false;
        }
        if (level.getServer() == null || level.getServer().isSingleplayer()) {
            return true;
        }
        VoteData data = new VoteManager(level).getData();
        LocalDateTime target = getScheduledOpeningTime(data);
        return target != null && !LocalDateTime.now().isBefore(target);
    }

    public static LocalDateTime getScheduledOpeningTime(VoteData data) {
        if (data == null || !data.isVoteFinished || data.selectedDate == null || data.selectedHour == null) {
            return null;
        }
        try {
            return LocalDate.parse(data.selectedDate).atTime(LocalTime.parse(data.selectedHour, HOUR_FORMAT));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static void setDebugEndMode(DebugEndMode mode) {
        debugEndMode = mode;
    }

    public enum DebugEndMode {
        NORMAL,
        OPEN,
        CLOSED
    }
}
