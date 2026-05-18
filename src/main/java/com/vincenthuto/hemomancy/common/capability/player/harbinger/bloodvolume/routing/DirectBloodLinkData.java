package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class DirectBloodLinkData {
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_MODE = "Mode";
    private static final String TAG_BLOODLINE = "Bloodline";
    private static final String TAG_RESERVE = "Reserve";

    private final UUID owner;
    private final BloodRoutingMode mode;
    private final boolean bloodlineEnabled;
    private final double workingReserve;

    public DirectBloodLinkData(UUID owner, BloodRoutingMode mode, boolean bloodlineEnabled, double workingReserve) {
        this.owner = owner;
        this.mode = mode;
        this.bloodlineEnabled = bloodlineEnabled;
        this.workingReserve = workingReserve;
    }

    public static DirectBloodLinkData nearby(UUID owner) {
        return new DirectBloodLinkData(owner, BloodRoutingMode.NEARBY, false, BloodRoutingRules.DEFAULT_WORKING_RESERVE);
    }

    public static DirectBloodLinkData load(CompoundTag tag) {
        if (tag == null || !tag.hasUUID(TAG_OWNER)) {
            return null;
        }
        BloodRoutingMode mode = BloodRoutingMode.NEARBY;
        if (tag.contains(TAG_MODE)) {
            try {
                mode = BloodRoutingMode.valueOf(tag.getString(TAG_MODE));
            } catch (IllegalArgumentException ignored) {
                mode = BloodRoutingMode.NEARBY;
            }
        }
        double reserve = tag.contains(TAG_RESERVE) ? tag.getDouble(TAG_RESERVE) : BloodRoutingRules.DEFAULT_WORKING_RESERVE;
        return new DirectBloodLinkData(tag.getUUID(TAG_OWNER), mode, tag.getBoolean(TAG_BLOODLINE), reserve);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_OWNER, owner);
        tag.putString(TAG_MODE, mode.name());
        tag.putBoolean(TAG_BLOODLINE, bloodlineEnabled);
        tag.putDouble(TAG_RESERVE, workingReserve);
        return tag;
    }

    public UUID owner() {
        return owner;
    }

    public BloodRoutingMode mode() {
        return mode;
    }

    public boolean bloodlineEnabled() {
        return bloodlineEnabled;
    }

    public double workingReserve() {
        return workingReserve;
    }

    public DirectBloodLinkData withMode(BloodRoutingMode newMode) {
        return new DirectBloodLinkData(owner, newMode, newMode == BloodRoutingMode.SANCTUM && bloodlineEnabled, workingReserve);
    }

    public DirectBloodLinkData withBloodlineEnabled(boolean enabled) {
        return new DirectBloodLinkData(owner, mode, enabled, workingReserve);
    }
}
