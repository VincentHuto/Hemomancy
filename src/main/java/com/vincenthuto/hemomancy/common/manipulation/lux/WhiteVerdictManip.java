package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WhiteVerdictManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 60;

	public WhiteVerdictManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		double range = 8.0D + 16.0D * charge;
		double width = 0.25D + 1.25D * charge;
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().normalize().scale(range));
		for (LivingEntity target : ManipulationCombatHelper.hostileTargets(player, level, range)) {
			if (ManipulationCombatHelper.distanceToSegment(target.getEyePosition(), eye, end) > width) continue;
			boolean concealed = target.isInvisible();
			target.removeEffect(MobEffects.INVISIBILITY);
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, true));
			ManipulationCombatHelper.hurt(this, player, target, level,
					(2.0F + 8.0F * charge) * (concealed ? 1.5F : 1.0F));
		}
		level.sendParticles(ParticleTypes.END_ROD, end.x, end.y, end.z, 18, width, width, width, 0.02D);
	}
}
