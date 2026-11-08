package com.peperehobbits01.createwaymod.util;

import com.peperehobbits01.createwaymod.data.VoteData;
import com.peperehobbits01.createwaymod.manager.VoteManager;
import net.minecraft.server.level.ServerLevel;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtils {
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static boolean isEndOpen(ServerLevel level) {
        if (level.getServer() == null || level.getServer().isSingleplayer()) {
            return true;
        }
        VoteData data = new VoteManager(level).getData();
        if (data == null || data.selectedDate == null || data.selectedHour == null) {
            return false;
        }

        try {
            LocalDateTime target = LocalDate.parse(data.selectedDate)
                .atTime(LocalTime.parse(data.selectedHour, HOUR_FORMAT));
            return !LocalDateTime.now().isBefore(target);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
