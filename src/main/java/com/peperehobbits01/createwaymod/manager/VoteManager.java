package com.peperehobbits01.createwaymod.manager;

import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.data.VoteData;
import net.minecraft.server.level.ServerLevel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class VoteManager {
    private static final String DATA_ID = "createway_vote_data";
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_VOTES_TO_FINISH = 4;
    public static final int MAX_DATES_PER_BALLOT = 5;

    private final ServerLevel level;

    public VoteManager(ServerLevel level) {
        this.level = level;
    }

    public VoteData getData() {
        if (level == null || level.getServer() == null || level.getServer().overworld() == null) {
            return null;
        }
        return level.getServer().overworld().getDataStorage().computeIfAbsent(VoteData.FACTORY, DATA_ID);
    }


    public void updateLogin(UUID uuid) {
        VoteData data = getData();
        if (data != null) {
            data.playerLastLogin.put(uuid, System.currentTimeMillis());
            data.setDirty();
        }
    }



    public VoteBatchResult castVotes(UUID uuid, List<String> dates) {
        if (!CreateWayConfig.isVoteSystemEnabled()) {
            return new VoteBatchResult(VoteStatus.UNAVAILABLE, 0, 0);
        }
        VoteData data = getData();
        if (data == null) return new VoteBatchResult(VoteStatus.UNAVAILABLE, 0, 0);
        if (data.isVoteFinished) return new VoteBatchResult(VoteStatus.CLOSED, 0, 0);
        if (dates == null || dates.isEmpty() || dates.size() > MAX_DATES_PER_BALLOT) {
            return new VoteBatchResult(VoteStatus.INVALID_DATES, 0, 0);
        }

        Set<String> uniqueDates = new LinkedHashSet<>(dates);
        if (uniqueDates.size() != dates.size()
            || uniqueDates.stream().anyMatch(date -> !isWithinVotingWindow(date))) {
            return new VoteBatchResult(VoteStatus.INVALID_DATES, 0, 0);
        }

        data.playerLastLogin.put(uuid, System.currentTimeMillis());
        int accepted = 0;
        int alreadyVoted = 0;
        for (String date : uniqueDates) {
            if (data.votes.computeIfAbsent(date, k -> new HashSet<>()).add(uuid)) {
                accepted++;
            } else {
                alreadyVoted++;
            }
        }

        if (accepted > 0) {
            checkThresholds(data);
        }
        data.setDirty();
        return new VoteBatchResult(VoteStatus.ACCEPTED, accepted, alreadyVoted);
    }



    public boolean setHour(String hour) {
        if (!CreateWayConfig.isVoteSystemEnabled()) {
            return false;
        }
        try {
            LocalTime.parse(hour, HOUR_FORMAT);
        } catch (DateTimeParseException e) {
            return false;
        }
        VoteData data = getData();
        if (data == null || data.selectedDate == null || !data.isVoteFinished) return false;
        data.selectedHour = hour;
        data.setDirty();
        return true;
    }

    public ReopenResult reopenVotes() {
        if (!CreateWayConfig.isVoteSystemEnabled()) {
            return ReopenResult.UNAVAILABLE;
        }
        VoteData data = getData();
        if (data == null) return ReopenResult.UNAVAILABLE;
        if (!data.isVoteFinished) return ReopenResult.NOT_CLOSED;

        data.votes.clear();
        data.selectedDate = null;
        data.selectedHour = null;
        data.isVoteFinished = false;
        data.setDirty();
        return ReopenResult.REOPENED;
    }


    private void checkThresholds(VoteData data) {
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        long activePlayers = data.playerLastLogin.values().stream()
                .filter(time -> time > sevenDaysAgo)
                .count();

        if (activePlayers == 0) return;

        for (Map.Entry<String, Set<UUID>> entry : data.votes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            String date = entry.getKey();
            Set<UUID> voters = entry.getValue();
            
            long requiredVotes = Math.max(MIN_VOTES_TO_FINISH, (long) Math.ceil(activePlayers * 0.75));
            if (voters.size() >= requiredVotes) {
                if (isWithinVotingWindow(date)) {
                    data.selectedDate = date;
                    data.isVoteFinished = true;
                    break;
                }
            }
        }
    }

    private boolean isWithinVotingWindow(String date) {
        if (date == null) {
            return false;
        }
        try {
            LocalDate voteDate = LocalDate.parse(date);
            LocalDate today = LocalDate.now();
            return !voteDate.isBefore(today.plusDays(7))
                && !voteDate.isAfter(today.plusMonths(2));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public enum VoteStatus {
        ACCEPTED,
        CLOSED,
        INVALID_DATES,
        UNAVAILABLE
    }

    public record VoteBatchResult(VoteStatus status, int accepted, int alreadyVoted) {
    }

    public enum ReopenResult {
        REOPENED,
        NOT_CLOSED,
        UNAVAILABLE
    }
}
