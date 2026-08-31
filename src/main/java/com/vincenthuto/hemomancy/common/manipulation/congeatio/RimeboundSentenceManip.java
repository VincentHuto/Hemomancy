package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
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

	public RimeboundSentenceManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		LivingEntity target = ManipulationCombatHelper.aimedTarget(player, level, 20, 0.75D);
		if (target == null) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		int duration = ManipulationScalingRules.scaledInt(20, 120, heldTicks, CHARGE_TICKS);
		ManipulationCombatHelper.hurt(this, player, target, level, 2.0F + 6.0F * charge);
		target.setTicksFrozen(Math.max(target.getTicksFrozen(), duration));
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration,
				ManipulationReactiveEvents.isBoss(target) ? 2 : 5, false, true));
		if (!ManipulationReactiveEvents.isBoss(target)) {
			BlockPos base = target.blockPosition();
			for (BlockPos offset : List.of(base.north(), base.south(), base.east(), base.west(), base.above(2))) {
				TemporaryIceManager.placeOwned(level, offset, Blocks.PACKED_ICE.defaultBlockState(), duration, player.getUUID());
			}
		}
	}
}
