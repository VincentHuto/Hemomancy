package com.vincenthuto.hemomancy.common.enchanting;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Legacy save format; migrated into StationUpgradeProgress on login. */
public final class ResonantForgeProgress implements INBTSerializable<CompoundTag> {
    private boolean taught, precisionClaimed, masterClaimed, precisionPending, masterPending;

    public boolean precisionClaimed() { return precisionClaimed; }
    public boolean masterClaimed() { return masterClaimed; }
    public boolean precisionPending() { return precisionPending; }
    public boolean masterPending() { return masterPending; }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Taught", taught);
        tag.putBoolean("PrecisionClaimed", precisionClaimed);
        tag.putBoolean("MasterClaimed", masterClaimed);
        tag.putBoolean("PrecisionPending", precisionPending);
        tag.putBoolean("MasterPending", masterPending);
        return tag;
    }

    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        taught = tag.getBoolean("Taught");
        precisionClaimed = tag.getBoolean("PrecisionClaimed");
        masterClaimed = tag.getBoolean("MasterClaimed");
        precisionPending = tag.getBoolean("PrecisionPending");
        masterPending = tag.getBoolean("MasterPending");
    }
}
