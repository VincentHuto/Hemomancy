package com.vincenthuto.hemomancy.common.brewing;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Legacy save format; migrated into StationUpgradeProgress on login. */
public final class AdvancedBrewingProgress implements INBTSerializable<CompoundTag> {
    private boolean distilled;
    private boolean refined;
    private boolean compounded;
    private boolean condenserClaimed;
    private boolean athanorClaimed;
    private boolean condenserPending;
    private boolean athanorPending;

    public boolean distilled() { return distilled; }
    public boolean refined() { return refined; }
    public boolean compounded() { return compounded; }
    public boolean condenserClaimed() { return condenserClaimed; }
    public boolean athanorClaimed() { return athanorClaimed; }
    public boolean condenserPending() { return condenserPending; }
    public boolean athanorPending() { return athanorPending; }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Distilled", distilled);
        tag.putBoolean("Refined", refined);
        tag.putBoolean("Compounded", compounded);
        tag.putBoolean("CondenserClaimed", condenserClaimed);
        tag.putBoolean("AthanorClaimed", athanorClaimed);
        tag.putBoolean("CondenserPending", condenserPending);
        tag.putBoolean("AthanorPending", athanorPending);
        return tag;
    }

    @Override public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        distilled = tag.getBoolean("Distilled");
        refined = tag.getBoolean("Refined");
        compounded = tag.getBoolean("Compounded");
        condenserClaimed = tag.getBoolean("CondenserClaimed");
        athanorClaimed = tag.getBoolean("AthanorClaimed");
        condenserPending = tag.getBoolean("CondenserPending");
        athanorPending = tag.getBoolean("AthanorPending");
    }
}
