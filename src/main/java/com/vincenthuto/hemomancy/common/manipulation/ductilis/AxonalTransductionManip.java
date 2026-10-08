package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AxonalTransductionManip extends BloodManipulation {
    public AxonalTransductionManip() {
        super("axonal_transduction", 150, 65, 0, EnumManipulationType.PASSIVE,
                EnumManipulationRank.PERFECTUS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD);
        setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges");
    }
    @Override protected boolean canPerformAction(Player player, ItemStack item, float ticks) {
        return player instanceof ServerPlayer server && AxonalTransductionManager.canEnter(server)
                && super.canPerformAction(player, item, ticks);
    }
    @Override public void getAction(Player player, Level level, ItemStack item, BlockPos pos, float ticks) {
        if (player instanceof ServerPlayer server) AxonalTransductionManager.begin(server);
    }
}
