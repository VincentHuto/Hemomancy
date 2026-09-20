package com.vincenthuto.hemomancy.common.succession;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public final class SuccessorRecord {
    public final UUID id, bloodline, donor, officiant, faneOwner;
    public final String profession, name, donorName, officiantName, dimension;
    public final long seed;
    public long workplace;
    public boolean displaced, dormant, dismissed;
    public int reserve = 1000;
    public long bloodspentUntil;
    public final CompoundTag encounters;

    public SuccessorRecord(CompoundTag tag) {
        id = tag.getUUID("Id"); bloodline = tag.getUUID("Bloodline"); donor = tag.getUUID("Donor");
        officiant = tag.getUUID("Officiant"); faneOwner = tag.getUUID("FaneOwner");
        profession = tag.getString("Profession"); name = tag.getString("Name");
        donorName = tag.getString("DonorName"); officiantName = tag.getString("OfficiantName");
        dimension = tag.getString("Dimension"); seed = tag.getLong("Seed"); workplace = tag.getLong("Workplace");
        displaced = tag.getBoolean("Displaced"); dormant = tag.getBoolean("Dormant"); dismissed = tag.getBoolean("Dismissed");
        reserve = tag.contains("Reserve") ? tag.getInt("Reserve") : 1000;
        bloodspentUntil = tag.getLong("BloodspentUntil"); encounters = tag.getCompound("Encounters").copy();
    }
    public SuccessionLedger.Workplace place() { return new SuccessionLedger.Workplace(dimension, workplace); }
    public CompoundTag save() {
        var t = new CompoundTag();
        t.putUUID("Id", id); t.putUUID("Bloodline", bloodline); t.putUUID("Donor", donor);
        t.putUUID("Officiant", officiant); t.putUUID("FaneOwner", faneOwner);
        t.putString("Profession", profession); t.putString("Name", name); t.putString("DonorName", donorName);
        t.putString("OfficiantName", officiantName); t.putString("Dimension", dimension);
        t.putLong("Seed", seed); t.putLong("Workplace", workplace); t.putBoolean("Displaced", displaced);
        t.putBoolean("Dormant", dormant); t.putBoolean("Dismissed", dismissed);
        t.putInt("Reserve", reserve); t.putLong("BloodspentUntil", bloodspentUntil); t.put("Encounters", encounters.copy());
        return t;
    }
}
