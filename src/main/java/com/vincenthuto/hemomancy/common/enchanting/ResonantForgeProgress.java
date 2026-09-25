package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class ResonantForgeProgress implements INBTSerializable<CompoundTag> {
    private boolean taught, precisionClaimed, masterClaimed, precisionPending, masterPending;

    public boolean taught() { return taught; }
    public boolean precisionClaimed() { return precisionClaimed; }
    public boolean masterClaimed() { return masterClaimed; }

    public boolean teach(ServerPlayer player) {
        if (taught || HemoCapabilityAccess.getPlayerDegreeNumber(player) < 3) return false;
        taught = true;
        return true;
    }

    public boolean canClaimPrecision(ServerPlayer player) {
        return taught && !precisionClaimed && HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 5;
    }

    public boolean canClaimMaster(ServerPlayer player) {
        return precisionClaimed && !masterClaimed && HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 7;
    }

    public boolean claimPrecision(ServerPlayer player) {
        if (!canClaimPrecision(player)) return false;
        precisionClaimed = precisionPending = true;
        deliver(player);
        return true;
    }

    public boolean claimMaster(ServerPlayer player) {
        if (!canClaimMaster(player)) return false;
        masterClaimed = masterPending = true;
        deliver(player);
        return true;
    }

    public void deliver(ServerPlayer player) {
        if (precisionPending) {
            ItemStack kit = new ItemStack(ItemInit.precision_governor_kit.get());
            player.getInventory().add(kit);
            if (kit.isEmpty()) precisionPending = false;
        }
        if (masterPending) {
            ItemStack kit = new ItemStack(ItemInit.master_cam_kit.get());
            player.getInventory().add(kit);
            if (kit.isEmpty()) masterPending = false;
        }
    }

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
