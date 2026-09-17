package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The two specimen containers share menu accounting, not their removal policies. */
public interface SpecimenContainer {
    CabinetStorage storage();
    BlockEntity blockEntity();
    void startOpen(Player player);
    void stopOpen(Player player);
    default boolean available() { return true; }
    default boolean beginTransfer() { return available(); }
    default void endTransfer() {}
}
