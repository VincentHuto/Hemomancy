package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.UUID;

public final class SanguineMarionetteManip extends BloodManipulation {
    public SanguineMarionetteManip() {
        super("sanguine_marionette", SanguineMarionetteRules.PULSE_COST, 0, 0, EnumManipulationType.CONTINUOUS,
                EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS);
        setSecondaryTend(EnumBloodTendency.DUCTILIS);
        setCooldownTicks(SanguineMarionetteRules.COOLDOWN);
        setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Requires a player's sustained aim");
    }

    private LivingEntity acquireTarget(ServerPlayer caster) {
        LivingEntity target = ManipulationCombatHelper.aimedTarget(caster, caster.level(), 8, .975);
        return target != null && HematicCommandManager.available(caster, target) ? target : null;
    }

    @Override public boolean canContinueChannel(Player player, Level world) {
        if (!(player instanceof ServerPlayer caster)) return false;
        if (HematicCommandManager.marionette(caster) != null) return HematicCommandManager.maintainMarionette(caster);
        return !ManipulationChannelManager.isChanneling(player.getUUID()) && acquireTarget(caster) != null;
    }

    @Override protected boolean canPerformAction(Player player, float charge) {
        return canContinueChannel(player, player.level());
    }

    @Override public void getAction(Player player, Level world, ItemStack held, BlockPos position, float charge) {
        if (!(player instanceof ServerPlayer caster)) return;
        if (HematicCommandManager.marionette(caster) == null) {
            LivingEntity target = acquireTarget(caster);
            if (target != null) HematicCommandManager.acquireMarionette(caster, target);
        }
    }

    @Override public void tickContinuousAction(Player player, Level world) {
        if (player instanceof ServerPlayer caster) HematicCommandManager.aimMarionette(caster);
    }

    @Override public void finishContinuousAction(Player player, boolean released) {
        HematicCommandManager.releaseMarionette(player.getUUID());
        super.finishContinuousAction(player, released);
    }

    @Override public void clearContinuousSession(UUID player) {
        HematicCommandManager.releaseMarionette(player);
    }
}
