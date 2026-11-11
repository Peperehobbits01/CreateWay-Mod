package com.peperehobbits01.createwaymod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoteData extends SavedData {
    private static final Logger LOGGER = LoggerFactory.getLogger("CreateWayMod.VoteData");

    public VoteData() {
        super();
        LOGGER.info("VoteData: New instance created via constructor");
    }

    public static final SavedData.Factory<VoteData> FACTORY = new SavedData.Factory<>(
        () -> {
            LOGGER.info("VoteData: Factory supplier called");
            return new VoteData();
        }, 
        (nbt, provider) -> {
            LOGGER.info("VoteData: Factory loader called");
            return VoteData.load(nbt, provider);
        }
    );

    public Map<UUID, Long> playerLastLogin = new ConcurrentHashMap<>();
    public Map<String, Set<UUID>> votes = new ConcurrentHashMap<>();
    public String selectedDate = null;
    public String selectedHour = null;
    public boolean isVoteFinished = false;
    public boolean openingAnnouncementSent = false;

    public static VoteData load(CompoundTag nbt, HolderLookup.Provider provider) {
        LOGGER.info("VoteData: load method entered");
        try {
            VoteData data = new VoteData();
            CompoundTag logins = nbt.getCompound("playerLastLogin");
            if (logins != null) {
                for (String key : logins.getAllKeys()) {
                    data.playerLastLogin.put(UUID.fromString(key), logins.getLong(key));
                }
            }

            CompoundTag allVotes = nbt.getCompound("votes");
            if (allVotes != null) {
                for (String date : allVotes.getAllKeys()) {
                    CompoundTag voters = allVotes.getCompound(date);
                    Set<UUID> set = new HashSet<>();
                    for (String uuid : voters.getAllKeys()) {
                        set.add(UUID.fromString(uuid));
                    }
                    data.votes.put(date, set);
                }
            }

            data.selectedDate = nbt.getString("selectedDate");
            if (data.selectedDate == null || data.selectedDate.isEmpty()) data.selectedDate = null;
            
            data.selectedHour = nbt.getString("selectedHour");
            if (data.selectedHour == null || data.selectedHour.isEmpty()) data.selectedHour = null;
            
            data.isVoteFinished = nbt.getBoolean("isVoteFinished");
            data.openingAnnouncementSent = nbt.getBoolean("openingAnnouncementSent");
            
            LOGGER.info("VoteData: load method completed successfully");
            return data;
        } catch (Exception e) {
            LOGGER.error("VoteData: Exception during load!", e);
            return new VoteData(); // Fallback to prevent null return
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.@NotNull Provider provider) {
        CompoundTag logins = new CompoundTag();
        playerLastLogin.forEach((uuid, time) -> logins.putLong(uuid.toString(), time));
        nbt.put("playerLastLogin", logins);

        CompoundTag allVotes = new CompoundTag();
        votes.forEach((date, voters) -> {
            CompoundTag vTag = new CompoundTag();
            voters.forEach(uuid -> vTag.putInt(uuid.toString(), 1));
            allVotes.put(date, vTag);
        });
        nbt.put("votes", allVotes);

        nbt.putString("selectedDate", selectedDate == null ? "" : selectedDate);
        nbt.putString("selectedHour", selectedHour == null ? "" : selectedHour);
        nbt.putBoolean("isVoteFinished", isVoteFinished);
        nbt.putBoolean("openingAnnouncementSent", openingAnnouncementSent);

        return nbt;
    }
}