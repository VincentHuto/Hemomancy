package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Set;

public final class AdvancedBrewingProgress implements INBTSerializable<CompoundTag> {
    private boolean distilled;
    private boolean refined;
    private boolean compounded;
    private boolean condenserClaimed;
    private boolean athanorClaimed;
    private boolean condenserPending;
    private boolean athanorPending;

    public void record(String operation) {
        switch (operation) {
            case "distill" -> distilled = true;
            case "refine" -> refined = true;
            case "compound" -> compounded = true;
            default -> { }
        }
    }

    public boolean distilled() { return distilled; }
    public boolean refined() { return refined; }
    public boolean compounded() { return compounded; }
    public boolean condenserClaimed() { return condenserClaimed; }
    public boolean athanorClaimed() { return athanorClaimed; }

    public boolean canClaimCondenser(ServerPlayer player) {
        if (condenserClaimed || HemoCapabilityAccess.getPlayerDegreeNumber(player) < 3 || !distilled) return false;
        boolean potion = false;
        Set<Item> enzymes = new java.util.HashSet<>();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.POTION) && BrewingResolver.source(stack, false) != null) potion = true;
            if (isEnzyme(stack)) enzymes.add(stack.getItem());
        }
        return potion && enzymes.size() >= 2;
    }

    public boolean canClaimAthanor(ServerPlayer player) {
        return !athanorClaimed && HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 5
                && refined && compounded;
    }

    public boolean claimCondenser(ServerPlayer player) {
        if (!canClaimCondenser(player)) return false;
        condenserClaimed = true;
        condenserPending = true;
        deliver(player);
        return true;
    }

    public boolean claimAthanor(ServerPlayer player) {
        if (!canClaimAthanor(player)) return false;
        athanorClaimed = true;
        athanorPending = true;
        deliver(player);
        return true;
    }

    public void deliver(ServerPlayer player) {
        if (condenserPending) {
            ItemStack kit = new ItemStack(ItemInit.hematic_condenser_kit.get());
            player.getInventory().add(kit);
            if (kit.isEmpty()) condenserPending = false;
        }
        if (athanorPending) {
            ItemStack kit = new ItemStack(ItemInit.sanguine_athanor_kit.get());
            player.getInventory().add(kit);
            if (kit.isEmpty()) athanorPending = false;
        }
    }

    private static boolean isEnzyme(ItemStack stack) {
        Item item = stack.getItem();
        return item == ItemInit.vivacious_enzyme.get() || item == ItemInit.ruinous_enzyme.get()
                || item == ItemInit.neurotic_enzyme.get() || item == ItemInit.ferric_enzyme.get()
                || item == ItemInit.fervent_enzyme.get() || item == ItemInit.frigid_enzyme.get()
                || item == ItemInit.incandescent_enzyme.get() || item == ItemInit.umbral_enzyme.get();
    }

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
