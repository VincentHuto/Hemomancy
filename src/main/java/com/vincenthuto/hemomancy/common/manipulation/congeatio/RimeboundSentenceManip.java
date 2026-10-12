package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.damage.*;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class RimeboundSentenceManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 70;
	/** Damage is BASE + CHARGE x charge fraction, before Crimson Mastery. */
	public static final float BASE_DAMAGE = 4.0F;
	public static final float CHARGE_DAMAGE = 8.0F;

	public RimeboundSentenceManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	private static LivingEntity target(Player player) {
		return ManipulationCombatHelper.aimedTarget(player, player.level(), 20, 0.75D);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player) == null) {
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("No vessel in your sights to sentence."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		LivingEntity target = target(player);
		if (target == null) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		int duration = ManipulationScalingRules.scaledInt(20, 120, heldTicks, CHARGE_TICKS);
		if (!ManipulationCombatHelper.hurt(this, player, target, level,
				(BASE_DAMAGE + CHARGE_DAMAGE * charge) * (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player), duration, charge >= 1 ? 3 : 1)) return;
        ManipulationVisuals.attached(target, ManipulationVisuals.Form.RIMEBOUND, .7, duration, 1);
		if (!ManipulationReactiveEvents.isBoss(target)) {
			BlockPos base = target.blockPosition();
			for (BlockPos offset : List.of(base.north(), base.south(), base.east(), base.west(), base.above(2))) {
				TemporaryIceManager.placeOwned(level, offset, BlockInit.frozen_cruor.get().defaultBlockState(), duration, player.getUUID());
			}
		}
	        }
    }
}
